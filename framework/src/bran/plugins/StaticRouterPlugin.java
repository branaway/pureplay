package bran.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jregex.Pattern;
import play.Play;
import play.Play.Mode;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.ControllersEnhancer.ControllerInstrumentation;
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Finally;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Route;
import play.mvc.Router;
import play.mvc.results.Result;
import play.utils.Java;
import bran.StaticActionInvoker;

public class StaticRouterPlugin extends PlayPlugin {
	private static final String DISPATCHER_FILENAME = "Dispatcher.java";
	public static final String STATIC_ROUTING_PKG = "staticRouting";
	private static final String STATIC_ROUTING_DIR = "conf/staticRouting";
	public static final String DISPATCHER_JAVA = STATIC_ROUTING_DIR + "/" + DISPATCHER_FILENAME;
	public static final String DISPATCHER_CLASS = "staticRouting.Dispatcher";

	static File appRoot = Play.applicationPath;
	public static File routerFile = new File(appRoot, DISPATCHER_JAVA);
	public static File staticRoutingDir = new File(appRoot, STATIC_ROUTING_DIR);

	private static final ArrayList<ApplicationClass> EMPTY_LIST = new ArrayList<ApplicationClass>();
	static boolean appClassChanged = false;
	static boolean routeChanged = false;

	boolean appStarted;
	boolean calledFromAppStart = false;

	long routeFileLastModified = -1;

	// long routerLastLoaded = -1;
	public static void clearRouteFile() {
		if (!routerFile.delete()) {
			System.out.println("! cannot delete the route file: " + DISPATCHER_JAVA);
		} else {
			// Play.classes.clearStaticActionInvoker();
		}
	}

	@Override
	public void onRoutesLoaded() {
		long lastLoading = Router.lastLoading;
		long lastModified = Play.routes.lastModified();

		if (Play.mode == Mode.PROD && lastLoading > 0) {
		} else {
			// System.out.println("route file last modified: " + lastModified);
			// System.out.println("Router last loaded: " + lastLoading);

			if (lastModified > routeFileLastModified) {
				System.out.println("route reloaded");
				routeChanged = true;
				// rebuildRouting(); // do it right away
			}
		}
		routeFileLastModified = lastModified;
		// routerLastLoaded = lastLoading;
	}

	// start or restart
	@Override
	public void onApplicationStart() {
		this.appStarted = true;
		this.calledFromAppStart = true;
		// rebuildRouting();
	}

	// // @Override
	// public void detectChange() {
	// if (appClassChanged || routeChanged) {
	// this.calledFromAppStart = false;
	// rebuildRouting();
	// }
	// }

	@Override
	public boolean rawInvocation(Request request, Response response) throws Exception {
		if (request.path.equals("/_rr") || request.path.equals("/_rebuildRouting")) {
			// ignore mode for now
			if (false && Play.mode == Mode.PROD) {
				String x = "cannot rebuild the routing table in PROD mode.";
				response.out.write(x.getBytes());
				return true;
			}

			clearRouteFile();
			Play.classes.remove("Dispatcher");
			Play.start();
			rebuild();
			reset();
			response.out.write("routing table has been rebuilt. See conf/Dispatcher.java".getBytes());
			return true;
		}
		return false;
	}

	// private void rebuildRouting() {
	// if (!appStarted)
	// return;
	// rebuild();
	// if (!calledFromAppStart) {
	// reset();
	// // will cause a restart
	// throw new RuntimeException("The routing table has been rebuilt.");
	// } else {
	// reset();
	// }
	// }

	@Override
	public void beforeDetectingChanges() {
		// if (!routerFile.exists()) {
		// rebuildRouting();
		// }
	}

	public static void rebuild() {
		if (!staticRoutingDir.exists()) {
			if (!staticRoutingDir.mkdirs()) {
				throw new RuntimeException("failed to mkdir: " + staticRoutingDir.getPath());
			}
		}

		System.out.println("Let's rebuild the static routing table");

		List<Route> routes = Router.routes;

		String ruleSwitches = "";
		for (int ruleCount = 0; ruleCount < routes.size(); ruleCount++) {
			Route r = routes.get(ruleCount);
			String ruleFile = translateRule(ruleCount, r);
			// for each rule we have:
			// else if ((r = GET_Application_index_sample.dispatch(invoke, req,
			// res)) != null) {return r;};
			ruleSwitches += "	else if ((r = " + ruleFile + ".dispatch(invoke, req, res)) != null) {return r;}\n";
		}

		String body = String.format(DISPATCHER_TEMPLATE, System.currentTimeMillis(), ruleSwitches);

		writeRouteFile(DISPATCHER_FILENAME, body);
	}

	/**
	 * @param ruleCount
	 * @param r
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String translateRule(int ruleCount, Route r) {
		// let generate a class name out of the route info
		StringBuilder sb = new StringBuilder();
		String string = r.toString();
		if (string.charAt(0) == '*')
			sb.append("Star");

		for (char c : string.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				sb.append(c);
			} else if (c == '/' || c == ' ' || c == '.') {
				sb.append('_');
			}
		}

		String ruleFileName = sb.toString();
		String codeInDispatch = "";

		String codeForAllControllerActions = "";
		if (r.isStatic()) {
			codeInDispatch += "throw new RuntimeException(\"The getMatches should have throw an RenderStatic exceptioin.\");\n";
		} else {

			String action = r.action;
			int lastDot = action.lastIndexOf('.');
			String controllerName = action.substring(0, lastDot);
			if (!controllerName.startsWith("controllers.")) {
				controllerName = "controllers\\." + controllerName;
			}
			String actionMethodName = action.substring(lastDot + 1);

			List<Class<? extends ControllerSupport>> controllerCandidates = new ArrayList<Class<? extends ControllerSupport>>();
			if (controllerName.contains("{")) {
				// parameterized controller name
				controllerName = controllerName.replaceAll("\\{([a-zA-Z_0-9]+)\\}", ".+");
				// should use the constraint in the path
				// for narrower scope
			}

			Pattern controllerPattern = new Pattern(controllerName);

			for (ApplicationClass ac : Play.classes.all()) {
				if (controllerPattern.matches(ac.name) && ac.javaClass != null) {
					controllerCandidates.add((Class<? extends ControllerSupport>) ac.javaClass);
				}
			}

			if (actionMethodName.contains("{")) {
				actionMethodName= actionMethodName.replaceAll("\\{([a-zA-Z_0-9]+)\\}", ".+");
				// should
				// use
				// the
				// constraint
				// in
				// the
				// path
				// for
				// narrower
				// scope
			}

			Map<String, String> controllerCode = new TreeMap<String, String>();
			for (Class<? extends ControllerSupport> c : controllerCandidates) {
				String code = codeForController(c, actionMethodName);
				codeForAllControllerActions += code;
				controllerCode.put(c.getName().replace('$', '.'), code);
			}

			for (String k : controllerCode.keySet()) {
				// something like for each item
				// else if (controllerName.equals("controllers.Application")) {
				// return controllers_Application(actionMethod);
				// }
				codeInDispatch += "			else if (controllerName.equals(\"" + k + "\")) {\n";
				codeInDispatch += "				return " + k.replace('.', '_') + "(actionMethod);\n";
				codeInDispatch += "			}\n";
			}
		}

		String ruleFileContent = String.format(RULE_IMPL_TEMPLATE, ruleFileName, ruleCount, codeInDispatch, codeForAllControllerActions);
		// let's create the java source code
		writeRouteFile(ruleFileName + ".java", ruleFileContent);
		return ruleFileName;
	}

	/**
	 * @param ruleFileName
	 * @param ruleFileContent
	 */
	private static void writeRouteFile(String ruleFileName, String ruleFileContent) {
		BufferedOutputStream bos;
		try {
			File target = new File(staticRoutingDir, ruleFileName);
			bos = new BufferedOutputStream(new FileOutputStream(target));
			bos.write(ruleFileContent.getBytes("UTF-8"));
			bos.close();
			System.out.println("[" + StaticRouterPlugin.class.getName() + "] router file updated: " + target.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create a method like:
	 * 
	 * private static Result controller_Apps(String actionMethod) { 
	 * if (actionMethod.equals("t2")) { 
	 * play.mvc.results.Result r = null;
	 * play.classloading
	 * .enhancers.ControllersEnhancer.ControllerInstrumentation.
	 * initActionCall(); r = controllers.Apps.t2(); return r; } else if
	 * (actionMethod.equals("aa")) { play.mvc.results.Result r = null;
	 * play.classloading
	 * .enhancers.ControllersEnhancer.ControllerInstrumentation.
	 * initActionCall(); r = controllers.Apps.aa(); return r; } return null; }
	 * 
	 * @param c
	 * @param actionMethodName
	 * @return
	 */
	private static String codeForController(Class<? extends ControllerSupport> c, String actionMethodName) {
		String cname = c.getName().replace('$', '_').replace('.', '_');

		Pattern methPattern = new Pattern(actionMethodName);
		Method[] methods = c.getMethods();
		List<Method> actions = new ArrayList<Method>();
		for (Method m : methods) {
			if (methPattern.matches(m.getName())) {
				if (Modifier.isStatic(m.getModifiers())) {
					if (Result.class.isAssignableFrom((Class<?>) m.getReturnType())) {
						if (!isInterceptor(m)) {
							actions.add(m);
						}
					}
				}
			}
		}

		String buf = new String("private static Result ");
		buf += cname + "(String actionMethod) { \n";
		buf += "	if (false){}\n";
		for (Method m : actions) {
			buf += "	else if (actionMethod.equals(\"" + m.getName() + "\")) {\n";
			buf += genActionCallCode(m) + "\n";
			buf += "}\n";
		}
		buf += "return null; }\n";
		return buf;
	}

	private static boolean isInterceptor(Method m) {
		Annotation[] as = m.getAnnotations();
		for (Annotation a : as) {
			if (a instanceof Before || a instanceof After || a instanceof Catch || a instanceof Finally  )
				return true;
		}
		return false;
	}

	static final String RULE_IMPL_TEMPLATE = "package staticRouting;\n" 
		+ "import java.util.Map;\n" 
		+ "import " + StaticActionInvoker.class.getName() + ";\n" 
			+ "import play.mvc.Http.Request;\n" + "import play.mvc.Http.Response;\n" + "import play.mvc.results.Result;\n"
			+ "import play.server.NettyInvocation;\n" + "import bran.StaticActionInvoker;\n" + "\n" + "public class %s {\n"
			+ "	public static Result dispatch(NettyInvocation invoke, Request req, Response res) {\n"
			+ "		Map<String, String> matches = StaticActionInvoker.getMatches(%s, req);\n" + "		if (matches != null) {\n"
			+ "			String actionMethod = req.actionMethod;\n" + "			String controllerName = req.controller;\n" + "			\n"
			+ "			if (false) {}\n" + "			// generated part\n" + "			%s  \n" + "			////\n" + "		}\n" + "		return null;\n" + "	}\n" + "\n"
			+ "\n" + "	// generated\n" + "	%s \n" + "	////\n" + "}\n" + "";

	/**
	 * generate code for the route.
	 * 
	 * @param r
	 * @param ruleNumber
	 *            the ordinal number of the route in the routes file
	 */
	public static void translateRoute(Route r, int ruleNumber) {

	}

	private void reset() {
		appClassChanged = false;
		routeChanged = false;
		Play.classes.clearStaticActionInvoker();
		calledFromAppStart = false;
		//
		// throw new RestartException();
	}

	public static void resetState() {
		appClassChanged = false;
		routeChanged = false;
		Play.classes.clearStaticActionInvoker();
	}

	@Override
	public List<ApplicationClass> onClassesChange(List<ApplicationClass> modified) {
		acChanged(modified);
		for (ApplicationClass c : modified) {
			System.out.println("class changed: " + c.name);
		}
		if (appClassChanged) {
			// rebuild(); // do it right away. XXX not right since the changed
			// files has not been compiled
			// // and the resulting router won't be correct if there is
			// signature change
			// reset();
		}
		// XXX shall we return the app class for the router?
		return EMPTY_LIST;
	}

	private void acChanged(List<ApplicationClass> modified) {
		for (ApplicationClass ac : modified)
			acChanged(ac);
	}

	public static boolean stateChanged() {
		return appClassChanged || routeChanged;
	}

	@Override
	public void onClassesRemoved(ApplicationClass c) {
		acChanged(c);
		if (appClassChanged)
			// rebuildRouting(); // do it right away
			System.out.println("class removed: " + c.name);
	}

	/**
	 * 
	 */
	private void acChanged(ApplicationClass applicationClass) {
		// Class<?> javaClass = applicationClass.javaClass;
		// sometime the java class is null. Let's use the package name
		// if (javaClass != null &&
		// ControllerSupport.class.isAssignableFrom(javaClass))
		// appClassChanged = true;

		if (applicationClass.name.startsWith("controllers."))
			appClassChanged = true;

	}

	@Override
	public void enhance(ApplicationClass applicationClass) throws Exception {
		// acChanged(applicationClass);
		System.out.println("enhanced: " + applicationClass.name);
	}

	static final String DISPATCHER_TEMPLATE = "package staticRouting;\n" + "\n" + "import play.mvc.Http.Request;\n"
			+ "import play.mvc.Http.Response;\n" + "import play.mvc.results.Result;\n" + "import play.server.NettyInvocation;\n"
			+ "import " + bran.StaticActionInvoker.class.getName() + ";\n" + "\n"
			+ "public class Dispatcher extends StaticActionInvoker {\n" + "	public final long version%s = 1; \n"
			+ "	protected Result dispatch(NettyInvocation invoke, Request req, Response res) {\n" + "		Result r = null;\r\n"
			+ "		if (false) {}\r\n" + "		// real work\n" + "		%s\n" // insertion
			// point
			+ " 		return null;\n" + "	}\n" + "\n"
			// point
			+ "	}\n";

	/**
	 * 
	 * @param classes
	 * @return
	 * @deprecated the catch all is just a normal rule translation
	 */
	private static String genCatchAll(ApplicationClasses classes) {
		List<ApplicationClass> controllers = null;
		controllers = classes.getAssignableClasses(ControllerSupport.class);

		Collections.sort(controllers, new Comparator<ApplicationClass>() {
			@Override
			public int compare(ApplicationClass o1, ApplicationClass o2) {
				return o1.name.compareTo(o2.name);
			}
		});

		StringBuilder sb = new StringBuilder();

		int c = controllers.size();
		for (int i = 0; i < c; i++) {
			ApplicationClass ac = controllers.get(i);
			Class<?> clz = ac.javaClass;
			String cname = clz.getName();

			String str = "if (controller.equals(\"" + cname + "\")) {";
			if (i == 0) {
				sb.append(str).append('\n');
			} else {
				sb.append("else " + str).append('\n');
			}

			Method[] methods = clz.getMethods();
			List<Method> actions = new ArrayList<Method>();
			for (Method m : methods) {
				if (Modifier.isStatic(m.getModifiers())) {
					if (Result.class.isAssignableFrom((Class<?>) m.getReturnType())) {
						actions.add(m);
					}
				}
			}

			for (int j = 0; j < actions.size(); j++) {
				Method a = actions.get(j);
				String str2 = "if (actionMethod.equals(\"" + a.getName() + "\"))  {";
				if (j == 0) {
					sb.append(str2).append('\n');
					// if (actionName.equals("${a.getName()}") {
				} else {
					sb.append("else " + str2).append('\n');
					// else if (actionName.equals("${a.getName()}") {
				}

				String actionCode = genActionCallCode(a);
				sb.append(actionCode);
				sb.append("}");

			}
			sb.append("}");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param method
	 * @return
	 * @throws Exception
	 */
	private static String genJustActionCall(Method method) {
		StringBuilder sb = new StringBuilder();
		String[] paramNames;
		try {
			paramNames = Java.parameterNames(method);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get the method paramnames. Not enhanced? ", e);
		}
		String cname = method.getDeclaringClass().getName();
		if (paramNames.length == 0) {
			String str3 = "	r = " + cname + "." + method.getName() + "();\n";
			sb.append(str3);
		} else {
			Class<?>[] parameterTypes = method.getParameterTypes();
			int p = 0;
			String params = "";
			for (Class<?> pt : parameterTypes) {
				if (pt.equals(int.class))
					pt = Integer.class;
				else if (pt.equals(long.class))
					pt = Long.class;
				else if (pt.equals(float.class))
					pt = Float.class;
				else if (pt.equals(double.class))
					pt = Double.class;
				else if (pt.equals(boolean.class))
					pt = Boolean.class;
				else if (pt.equals(char.class))
					pt = Character.class;

				String paramTypeName = pt.getName().replace('$', '.');
				String pname = paramNames[p];
				sb.append(pt.getName() + " " + pname + " = (" + paramTypeName + ") " + StaticActionInvoker.class.getName() + ".getArg(\"" + pname + "\", " + paramTypeName + ".class"
						+ ");\n");
				params += pname + (p == (parameterTypes.length - 1) ? "" : ", ");
				p++;
			}

			sb.append("	r = " + cname + "." + method.getName() + "(" + params + ");\n");
		}
		return sb.toString();
	}

	public static class ActionMethodArg {
		public String name;
		public Class<?> type;
		public Annotation annotation;

		public ActionMethodArg(String name, Class<?> type, Annotation annotation) {
			super();
			this.name = name;
			this.type = type;
			this.annotation = annotation;
		}
	}

	// must clear the content after each code regen run
	static Map<Class<? extends ControllerSupport>, List<Method>> controllerBefores = new HashMap<Class<? extends ControllerSupport>, List<Method>>();
	static Map<Class<? extends ControllerSupport>, List<Method>> controllerAfters = new HashMap<Class<? extends ControllerSupport>, List<Method>>();
	static Map<Class<? extends ControllerSupport>, List<Method>> controllerFinallies = new HashMap<Class<? extends ControllerSupport>, List<Method>>();

	static void sortActionInterceptors(Method meth) {
		@SuppressWarnings("unchecked")
		Class<? extends ControllerSupport> cls = (Class<? extends ControllerSupport>) meth.getDeclaringClass();
		extractAnnotations(cls);
		String action = meth.getName();
		actionBefores.put(meth, extractInterceptor(action, controllerBefores.get(cls), Before.class));
		actionAfters.put(meth, extractInterceptor(action, controllerAfters.get(cls), After.class));
		actionFinallies.put(meth, extractInterceptor(action, controllerFinallies.get(cls), Finally.class));
	}

	private static String genActionCallCode(Method m) {
		String resultClassName = Result.class.getName();
		String controllerInstClassName = ControllerInstrumentation.class.getName().replace('$', '.');

		StringBuilder sb = new StringBuilder();

		sb.append("					" + resultClassName + " r = null;\n");

		List<Method> befores = getActionBefores(m);

		if (befores.size() > 0)
			sb.append("	                " + controllerInstClassName + ".stopActionCall();\n");

		for (Method b : befores) {
			Class<?> c = b.getDeclaringClass();
			String cname = c.getName();
			sb.append("	r = " + cname + "." + b.getName() + "();\n");
			sb.append("		if (r != null)\n" + "			return r;\n");
		}

		sb.append("            " + controllerInstClassName + ".initActionCall();\n");
		sb.append(genJustActionCall(m));

		List<Method> afters = actionAfters.get(m);
		if (afters.size() > 0) {
			sb.append("	                " + controllerInstClassName + ".stopActionCall();\n");
			sb.append("					" + resultClassName + " ra = null;\n");
		}

		for (Method a : afters) {
			Class<?> c = a.getDeclaringClass();
			String cname = c.getName();
			sb.append("	ra = " + cname + "." + a.getName() + "();\n");
			sb.append("		if (ra != null)\n" + "			return ra;\n");
		}

		// TODO: catch, finally..

		sb.append("	return r;\n");

		return sb.toString();
	}

	/**
	 * @param m
	 * @return
	 */
	private static List<Method> getActionBefores(Method m) {
		sortActionInterceptors(m);
		return actionBefores.get(m);
	}

	public void resetCache() {
		controllerBefores.clear();
		controllerAfters.clear();
		controllerFinallies.clear();

		actionBefores.clear();
		actionAfters.clear();
		actionFinallies.clear();

	}

	/**
	 * @param action
	 * @param befores
	 * @return
	 */
	private static <T extends Annotation> List<Method> extractInterceptor(String action, List<Method> befores, Class<T> clz) {
		List<Method> beforesToRun = new ArrayList<Method>();
		for (Method before : befores) {
			T annotation = before.getAnnotation(clz);
			String[] unless = null;
			String[] only = null;
			// too bad annotations don't support abstraction

			if (clz == Before.class) {
				unless = ((Before) annotation).unless();
				only = ((Before) annotation).only();
			}

			if (clz == After.class) {
				unless = ((After) annotation).unless();
				only = ((After) annotation).only();
			}

			if (clz == Finally.class) {
				unless = ((Finally) annotation).unless();
				only = ((Finally) annotation).only();
			}

			for (String un : only) {
				if (!un.contains(".")) {
					un = before.getDeclaringClass().getName().substring(12).replace("$", "") + "." + un;
				}
				if (un.equals(action)) {
					beforesToRun.add(before);
				}
			}

			boolean match = true;

			for (String un : unless) {
				if (!un.contains(".")) {
					un = before.getDeclaringClass().getName().substring(12).replace("$", "") + "." + un;
				}
				if (un.equals(action)) {
					match = false;
					break;
				}
			}
			if (match) {
				beforesToRun.add(before);
			}
		}
		return beforesToRun;
	}

	static Map<Method, List<Method>> actionBefores = new HashMap<Method, List<Method>>();
	static Map<Method, List<Method>> actionAfters = new HashMap<Method, List<Method>>();
	static Map<Method, List<Method>> actionFinallies = new HashMap<Method, List<Method>>();

	static private void extractAnnotations(Class<? extends ControllerSupport> cls) {
		{
			List<Method> beforeList = controllerBefores.get(cls);
			if (beforeList == null) {
				List<Method> befores = Java.findAllAnnotatedMethods(cls, Before.class);
				Collections.sort(befores, new Comparator<Method>() {
					@Override
					public int compare(Method m1, Method m2) {
						Before before1 = m1.getAnnotation(Before.class);
						Before before2 = m2.getAnnotation(Before.class);
						return before1.priority() - before2.priority();
					}
				});
				controllerBefores.put(cls, befores);
			}
		}

		{
			List<Method> afterList = controllerAfters.get(cls);
			if (afterList == null) {
				List<Method> afters = Java.findAllAnnotatedMethods(cls, After.class);
				Collections.sort(afters, new Comparator<Method>() {
					@Override
					public int compare(Method m1, Method m2) {
						Before before1 = m1.getAnnotation(Before.class);
						Before before2 = m2.getAnnotation(Before.class);
						return before1.priority() - before2.priority();
					}
				});
				controllerAfters.put(cls, afters);
			}
		}

		{
			List<Method> finallyList = controllerFinallies.get(cls);
			if (finallyList == null) {
				List<Method> fianllies = Java.findAllAnnotatedMethods(cls, Finally.class);
				Collections.sort(fianllies, new Comparator<Method>() {
					@Override
					public int compare(Method m1, Method m2) {
						Before before1 = m1.getAnnotation(Before.class);
						Before before2 = m2.getAnnotation(Before.class);
						return before1.priority() - before2.priority();
					}
				});
				controllerFinallies.put(cls, fianllies);
			}
		}

		// let's sort out all the action interceptors
		Method[] methods = cls.getMethods();
		List<Method> actions = new ArrayList<Method>();
		for (Method m : methods) {
			if (Modifier.isStatic(m.getModifiers())) {
				if (Result.class.isAssignableFrom((Class<?>) m.getReturnType())) {
					actions.add(m);
				}
			}
		}

	}
}
