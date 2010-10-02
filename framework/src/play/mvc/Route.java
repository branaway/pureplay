package play.mvc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import play.Logger;
import play.mvc.results.NotFound;
import play.mvc.results.RenderStatic;

public class Route {

    public String method;
    public String path;
    public String action;
    Pattern actionPattern;
    List<String> actionArgs = new ArrayList<String>();
    String staticDir;
    Pattern pattern;
    Pattern hostPattern;
    List<Route.Arg> args = new ArrayList<Route.Arg>();
    Map<String, String> staticArgs = new HashMap<String, String>();
    List<String> formats = new ArrayList<String>();
    String host;
    Route.Arg hostArg = null;
    public int routesFileLine;
    public String routesFile;
    static Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_0-9]+)\\}");
    static Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");
    static Pattern paramPattern = new Pattern("([a-zA-Z_0-9]+):'(.*)'");

    public void compute() {
        this.host = ".*";
        this.hostPattern = new Pattern(host);
        // staticDir
        if (action.startsWith("staticDir:")) {
            if (!method.equalsIgnoreCase("*") && !method.equalsIgnoreCase("GET")) {
                Logger.warn("Static route only support GET method");
                return;
            }
            if (!this.path.endsWith("/") && !this.path.equals("/")) {
                Logger.warn("The path for a staticDir route must end with / (%s)", this);
                this.path += "/";
            }
            this.pattern = new Pattern("^" + path + "({resource}.*)$");
            this.staticDir = action.substring("staticDir:".length());
        } else {
            // URL pattern
            // Is there is a host argument, append it.
            if (!path.startsWith("/")) {
                String p = this.path;
                this.path = p.substring(p.indexOf("/"));
                this.host = p.substring(0, p.indexOf("/"));

                Matcher m = new Pattern(".*\\{({name}.*)\\}.*").matcher(host);

                if (m.matches()) {
                    String name = m.group("name");
                    hostArg = new Arg();
                    hostArg.name = name;
                }
                this.host = this.host.replaceFirst("\\{.*\\}", "");
                this.hostPattern = new Pattern(host);
            }
            String patternString = path;
            patternString = customRegexPattern.replacer("\\{<[^/]+>$1\\}").replace(patternString);
            Matcher matcher = argsPattern.matcher(patternString);
            while (matcher.find()) {
                Route.Arg arg = new Arg();
                arg.name = matcher.group(2);
                arg.constraint = new Pattern(matcher.group(1));
                args.add(arg);
            }

            patternString = argsPattern.replacer("({$2}$1)").replace(patternString);
            this.pattern = new Pattern(patternString);
            // Action pattern
            patternString = action;
            patternString = patternString.replace(".", "[.]");
            for (Route.Arg arg : args) {
                if (patternString.contains("{" + arg.name + "}")) {
                    patternString = patternString.replace("{" + arg.name + "}", "({" + arg.name + "}" + arg.constraint.toString() + ")");
                    actionArgs.add(arg.name);
                }
            }
            actionPattern = new Pattern(patternString, REFlags.IGNORE_CASE);
        }
    }

    public void addParams(String params) {
        if (params == null || params.length() < 1) {
            return;
        }
        params = params.substring(1, params.length() - 1);
        for (String param : params.split(",")) {
            Matcher matcher = paramPattern.matcher(param);
            if (matcher.matches()) {
                staticArgs.put(matcher.group(1), matcher.group(2));
            } else {
                Logger.warn("Ignoring %s (static params must be specified as key:'value',...)", params);
            }
        }
    }

    // TODO: Add args names
    public void addFormat(String params) {
        if (params == null || params.length() < 1) {
            return;
        }
        params = params.trim();
        for (String param : params.split(",")) {
            formats.add(param);
        }
    }

    private boolean contains(String accept) {
        boolean contains = (accept == null);
        if (accept != null) {
            if (this.formats.size() == 0) {
                return true;
            }
            for (String format : this.formats) {
                contains = format.startsWith(accept);
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }

    public Map<String, String> matches(String method, String path) {
        return matches(method, path, null, null);
    }

    public Map<String, String> matches(String method, String path, String accept) {
        return matches(method, path, accept, null);
    }

    /**
     * 
     * @param method
     * @param path
     * @param accept
     * @param host
     * @return a map of parameter name and values in string
     */
    public Map<String, String> matches(String method, String path, String accept, String host) {
        // If method is HEAD and we have a GET
        if (method == null || this.method.equals("*") || method.equalsIgnoreCase(this.method) || (method.equalsIgnoreCase("head") && ("get").equalsIgnoreCase(this.method))) {

            Matcher matcher = pattern.matcher(path);

            boolean hostMatches = (host == null);
            if (host != null) {
                Matcher hostMatcher = hostPattern.matcher(host);
                hostMatches = hostMatcher.matches();
            }
            // Extract the host variable
            if (matcher.matches() && contains(accept) && hostMatches) {
                // Static dir
                if (staticDir != null) {
                    String resource = matcher.group("resource");
                    try {
                        String root = new File(staticDir).getCanonicalPath();
                        String child = new File(staticDir + "/" + resource).getCanonicalPath();
                        if (child.startsWith(root)) {
                            throw new RenderStatic(staticDir + "/" + resource);
                        }
                    } catch (IOException e) {
                    }
                    throw new NotFound(resource);
                } else {
                    Map<String, String> localArgs = new HashMap<String, String>();
                    for (Route.Arg arg : args) {
                        localArgs.put(arg.name, matcher.group(arg.name));
                    }
                    if (hostArg != null && host != null) {
                        localArgs.put(hostArg.name, host);
                    }
                    localArgs.putAll(staticArgs);
                    return localArgs;
                }
            }
        }
        return null;
    }

    static class Arg {

        String name;
        Pattern constraint;
        String defaultValue;
        Boolean optional = false;
    }

    @Override
    public String toString() {
        return method + " " + path + " -> " + action;
    }
}