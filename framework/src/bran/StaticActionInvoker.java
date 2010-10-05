package bran;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import play.classloading.enhancers.ControllersEnhancer.ControllerInstrumentation;
import play.data.binding.Binder;
import play.data.parsing.UrlEncodedParser;
import play.i18n.Lang;
import play.mvc.ActionInvoker;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Route;
import play.mvc.Router;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.results.NotFound;
import play.mvc.results.Result;
import play.server.NettyInvocation;

/**
 * In contrast to {@link ActionInvoker}, concrete instance of this interface is
 * generated at runtime from the route file and the route rules are translated
 * to static action invocations for the maximum performance.
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public abstract class StaticActionInvoker {
	static Route defaultRoute = new Route();
	static {
		defaultRoute.method = "*";
		defaultRoute.path = "/{controller}/{action}";
		defaultRoute.action = "{controller}.{action}";
		defaultRoute.compute();
	}

	public Result invoke(NettyInvocation invoke, Request req, Response res) {
		Http.Request.current.set(req);
		Http.Response.current.set(res);

		Scope.Params.current.set(new Scope.Params());
		Scope.RenderArgs.current.set(new Scope.RenderArgs());
		Scope.RouteArgs.current.set(new Scope.RouteArgs());
		Scope.Session.current.set(Scope.Session.restore());
		Scope.Flash.current.set(Scope.Flash.restore());

		try {
			ControllerInstrumentation.initActionCall();
			Result dispatch = dispatch(invoke, req, res);
			return dispatch;
		} finally {
			Scope.Session.current().save();
			Scope.Flash.current().save();
		}
	}

	protected abstract Result dispatch(NettyInvocation invoke, Request req, Response res);

	// protected abstract Result catchAll(NettyInvocation invoke, Request req,
	// Response res);

	/**
	 * @param req
	 */
	protected boolean parseDefaultReqFormat(Request req) {
		Map<String, String> args = defaultRoute.matches(req.method, req.path, req.format, req.host);
		if (args != null) {
			req.routeArgs = args;
			req.action = defaultRoute.action;
			if (args.containsKey("format")) {
				req.format = args.get("format");
			}
			if (req.action.indexOf("{") > -1) { // more optimization ?
				for (String arg : req.routeArgs.keySet()) {
					req.action = req.action.replace("{" + arg + "}", req.routeArgs.get(arg));
				}
			}

			Params currentParams = Scope.Params.current();
			currentParams.__mergeWith(req.routeArgs);
			// add parameters from the URI query string
			try {
				currentParams._mergeWith(UrlEncodedParser.parseQueryString(new ByteArrayInputStream(req.querystring.getBytes("utf-8"))));
			} catch (UnsupportedEncodingException e) {
			}
			Lang.resolvefrom(req);

			return true;
		}
		return false;
	}

	protected Object getArg(Request req, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Map<String, String> getMatches(int i, Request req) {
		return getMatches(Router.routes.get(i), req);
	}

	/**
	 * @param req
	 * @return
	 */
	protected static Map<String, String> getMatches(Route r, Request req) {
		Map<String, String> args = r.matches(req.method, req.path, req.format, req.host);
		if (args != null) {
			req.routeArgs = args;
			req.action = r.action;
			if (args.containsKey("format")) {
				req.format = args.get("format");
			}
			if (req.action.indexOf("{") > -1) { // more optimization ?
				for (String arg : req.routeArgs.keySet()) {
					req.action = req.action.replace("{" + arg + "}", req.routeArgs.get(arg));
				}
			}
			int lastDot = req.action.lastIndexOf('.');

			String controllerName = req.action.substring(0, lastDot);
			if (!controllerName.startsWith("controllers.")) {
				controllerName = "controllers." + controllerName;
			}
			String actionMethodName = req.action.substring(lastDot + 1);

			req.actionMethod = actionMethodName;
			req.controller = controllerName;

			Params currentParams = Scope.Params.current();
			currentParams.__mergeWith(req.routeArgs);
			// add parameters from the URI query string
			try {
				currentParams._mergeWith(UrlEncodedParser.parseQueryString(new ByteArrayInputStream(req.querystring.getBytes("utf-8"))));
			} catch (UnsupportedEncodingException e) {
			}
			Lang.resolvefrom(req);

		}
		return args;
	}

	public static <T> T getArg(String paramName, Class<T> type) {
		return getArg(paramName, type, new Annotation[] {});
	}

	/**
	 * 
	 * @param <T>
	 * @param paramName
	 * @param type
	 * @param paramConstraints
	 * @return
	 */
	public static <T> T getArg(String paramName, Class<T> type, Annotation[] paramConstraints) {
		Map<String, String[]> params = new HashMap<String, String[]>();

		Params currentParams = Scope.Params.current();

		if (type.equals(String.class) || Number.class.isAssignableFrom(type) || type.isPrimitive()) {
			params.put(paramName, currentParams.getAll(paramName));
		} else {
			params.putAll(currentParams.all());
		}

		@SuppressWarnings("unchecked")
		T result = (T) Binder.bind(paramName, type, null, paramConstraints, params, null, null, 0);
		return result;
	}

}
