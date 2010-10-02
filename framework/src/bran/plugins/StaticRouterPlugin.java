package bran.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import play.Play;
import play.Play.Mode;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.mvc.results.Result;
import play.utils.Java;
import bran.RestartException;

public class StaticRouterPlugin extends PlayPlugin {
	public static final String DISPATCHER_JAVA = "conf/Dispatcher.java";
	public static final String DISPATCHER_CLASS = "Dispatcher";
	
	static File appRoot = Play.applicationPath;
	public static File routerFile = new File(appRoot, DISPATCHER_JAVA);

	
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
		}
		else {
//			Play.classes.clearStaticActionInvoker();
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
//				rebuildRouting(); // do it right away
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

//	// @Override
//	 public void detectChange() {
//	 if (appClassChanged || routeChanged) {
//	 this.calledFromAppStart = false;
//	 rebuildRouting();
//	 }
//	 }

	@Override
	public boolean rawInvocation(Request request, Response response) throws Exception {
		if (request.path.equals("/_rr") || request.path.equals("/_rebuildRouting")) {
			if (Play.mode == Mode.PROD) {
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

	private void rebuildRouting() {
		if (!appStarted)
			return;
		rebuild();
		if (!calledFromAppStart) {
			reset();
			// will cause a restart
			throw new RuntimeException("The routing table has been rebuilt.");
		} else {
			reset();
		}
	}

	@Override
	public void beforeDetectingChanges() {
//		if (!routerFile.exists()) {
//			rebuildRouting();
//		}
	}


	public static void rebuild() {
		System.out.println("Let's rebuild the static routing table");
		String catchall = genCatchAll(Play.classes);

		
		String body = String.format(DISPATCHER_TEMPLATE, System.currentTimeMillis(), "//TODO", catchall);
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(routerFile));
			bos.write(body.getBytes("UTF-8"));
			bos.close();
			System.out.println("router file updated: " + routerFile.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void reset() {
		appClassChanged = false;
		routeChanged = false;
		Play.classes.clearStaticActionInvoker();
		calledFromAppStart = false;
		// 
//		throw new RestartException();
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
		if(appClassChanged) {
//			rebuild(); // do it right away. XXX not right since the changed files has not been compiled
//			// and the resulting router won't be correct if there is signature change
//			reset();
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
//			rebuildRouting(); // do it right away
		System.out.println("class removed: " + c.name);
	}

//	@Override
//	public Collection<? extends ApplicationClass> onNewlyCompiled(Set<ApplicationClass> applicationClasses) {
//		Iterator<ApplicationClass> iterator = applicationClasses.iterator();
//		while (iterator.hasNext()) {
//			ApplicationClass ac = iterator.next();
//			Class<?> javaClass = ac.javaClass;
//			if (javaClass != null && ControllerSupport.class.isAssignableFrom(javaClass)) {
//				rebuild();
//				List<ApplicationClass> assignableClasses = Play.classes.getAssignableClasses(StaticActionInvoker.class);
//				for (ApplicationClass c : assignableClasses) {
//					c.refresh();
//				}
//				return assignableClasses;
//			}
//		}
//		return Collections.emptyList();
//	}

	/**
	 * 
	 */
	private void acChanged(ApplicationClass applicationClass) {
//		Class<?> javaClass = applicationClass.javaClass;
		// sometime the java class is null. Let's use the package name
//		if (javaClass != null && ControllerSupport.class.isAssignableFrom(javaClass))
//			appClassChanged = true;
		
		if(applicationClass.name.startsWith("controllers."))
			appClassChanged = true;
			
	}

	@Override
	public void enhance(ApplicationClass applicationClass) throws Exception {
		// acChanged(applicationClass);
		System.out.println("enhanced: " + applicationClass.name);
	}

	static final String DISPATCHER_TEMPLATE = "/*package router;*/\r\n" + "\r\n" + "import play.mvc.Http.Request;\r\n"
			+ "import play.mvc.Http.Response;\r\n" + "import play.mvc.results.Result;\r\n" + "import play.server.NettyInvocation;\r\n"
			+ "import "
			+ bran.StaticActionInvoker.class.getName()
			+ ";\n"
			+ "\r\n"
			+ "public class Dispatcher extends StaticActionInvoker {\r\n"
			+ "	public final long version%s = 1; \r\n"
			+ "	protected Result dispatch(NettyInvocation invoke, Request req, Response res) {\r\n"
			+ "		// real work\r\n"
			+ "		%s\r\n"
			+ // insertion point
			"		return null;\r\n"
			+ "	}\r\n"
			+ "\r\n"
			+ "	/**\r\n"
			+ "	 * the translation of * /{controller}/{action} {controller}.{action}\r\n"
			+ "	 * \r\n"
			+ "	 * @param invoke\r\n"
			+ "	 * @param req\r\n"
			+ "	 * @param res\r\n"
			+ "	 */\r\n"
			+ "	protected Result catchAll(NettyInvocation invoke, Request req, Response response) {\r\n"
			+ "		// return a map of parameter name and values in string\r\n"
			+ "		if (parseDefaultReqFormat(req)) {\r\n"
			+ "			String action = req.action; // the full action\r\n"
			+ "			int lastDot = action.lastIndexOf('.');\r\n"
			+ "			String controller = action.substring(0, lastDot);\r\n"
			+ "			if (!controller.startsWith(\"controllers.\")) \r\n"
			+ "				controller = \"controllers.\" + controller; \r\n"
			+ "			String actionMethod = action.substring(lastDot + 1);\r\n"
			+ "\r\n" + "\r\n" + "		// real work\r\n" + "		%s \r\n" + // insertion
			// point
			"		\r\n" + "		}\r\n" + "		return null;\r\n" + "\r\n" + "	}\r\n" + "}\r\n" + "";

	public static String genCatchAll(ApplicationClasses classes) {
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

				try {
					String[] paramNames = Java.parameterNames(a);
					if (paramNames.length == 0) {
						sb.append("return " + cname + "." + a.getName() + "();").append('\n');
					} else {
						Class<?>[] parameterTypes = a.getParameterTypes();
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

							String pname = paramNames[p];
							sb.append(pt.getName() + " " + pname + " = (" + pt.getName() + ") getArg(req, " + "\"" + pname + "\");\n");
							params += pname + (p == (parameterTypes.length - 1) ? "" : ", ");
							p++;
						}

						sb.append("return " + cname + "." + a.getName() + "(" + params + ");\n");
					}

					sb.append("}");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			sb.append("}");
		}
		return sb.toString();
	}
	
	
	
}
