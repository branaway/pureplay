package play.mvc;

import java.util.Map;

public class ActionDefinition {

    public String method;
    public String url;
    public boolean star;
    public String action;
    public Map<String, Object> args;

    public ActionDefinition add(String key, Object value) {
        args.put(key, value);
        return Router.reverse(action, args);
    }

    public ActionDefinition remove(String key) {
        args.remove(key);
        return Router.reverse(action, args);
    }

    public ActionDefinition addRef(String fragment) {
        url += "#" + fragment;
        return this;
    }

    @Override
    public String toString() {
        return url;
    }

    public void absolute() {
        url = Http.Request.current().getBase() + url;
    }

    public ActionDefinition secure() {
        if (url.contains("http://")) {
            url = url.replace("http:", "https:");
            return this;
        }
        url = "https://" + Http.Request.current().host + url;
        return this;
    }
}