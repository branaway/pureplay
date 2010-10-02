package bran;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.ControllersEnhancer.ControllerInstrumentation;
import play.mvc.ActionInvoker;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Route;
import play.mvc.Scope;
import play.mvc.results.Result;
import play.server.NettyInvocation;
import play.utils.Java;

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
			if (dispatch != null)
				return dispatch;
			else {
				return catchAll(invoke, req, res);
			}
		} catch (Result result) {
			Scope.Session.current().save();
			Scope.Flash.current().save();
			// now dump the result to the response
			result.apply(req, res);
		} finally {
		}
		return null;
	}

	protected abstract Result dispatch(NettyInvocation invoke, Request req, Response res);
	protected abstract Result catchAll(NettyInvocation invoke, Request req, Response res);


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
			return true;
		}
		return false;
	}

	protected Object getArg(Request req, String string) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
