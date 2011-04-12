package play.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import play.Invocation;
import play.Logger;
import play.Play;
import play.mvc.ActionInvoker;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.mvc.results.NotFound;
import play.mvc.results.RenderStatic;

public class NettyInvocationClassic extends Invocation {
	static ConcurrentHashMap<String, RenderStatic> staticPathsCache = new ConcurrentHashMap<String, RenderStatic>();
	/**
	 * 
	 */
	private final ChannelHandlerContext ctx;
	private final Request request;
	private final Response response;
	private final HttpRequest nettyRequest;
	private final MessageEvent e;

	// bran expose two internal objects for easier handling
	public Request getRequest() {
		return request;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public NettyInvocationClassic(Request request, Response response, ChannelHandlerContext ctx, HttpRequest nettyRequest, MessageEvent e) {
		this.ctx = ctx;
		this.request = request;
		this.response = response;
		this.nettyRequest = nettyRequest;
		this.e = e;
	}

	@Override
	public boolean init() {
		Logger.trace("init: begin");
		Request.current.set(request);
		Response.current.set(response);
		// Patch favicon.ico
		if (!request.path.equals("/favicon.ico")) {
			super.init();
		}
		if (Play.mode == Play.Mode.PROD) {
			RenderStatic rs = staticPathsCache.get(request.path);
			if (rs != null) {
//				PlayHandler.serveStatic(rs, ctx, request, response, nettyRequest, e);
				PlayHandler.serveStatic(rs.file, ctx, request, response, nettyRequest);
				Logger.trace("init: end false");
				return false;
			}
		}
		try {
			Router.routeOnlyStatic(request);
		} catch (NotFound e) {
			PlayHandler.serve404(e, ctx, request, nettyRequest);
			Logger.trace("init: end false");
			return false;
		} catch (RenderStatic e) {
			if (Play.mode == Play.Mode.PROD) {
					staticPathsCache.put(request.path, e);
			}
//			PlayHandler.serveStatic(e, ctx, request, response, nettyRequest, this.e);
			PlayHandler.serveStatic(e.file, ctx, request, response, nettyRequest);
			Logger.trace("init: end false");
			return false;
		}
		Logger.trace("init: end true");
		return true;
	}

	@Override
	public void run() {
		try {
			Logger.trace("run: begin");
			super.run();
		} catch (Exception e) {
			PlayHandler.serve500(e, ctx, nettyRequest);
		}
		Logger.trace("run: end");
	}

	/**
	 * write arbitrary string to response
	 * 
	 * @param string
	 * @author bran
	 */
	private void respond(String string) {
		try {
			byte[] bytes;
			bytes = string.getBytes("utf-8");

			HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

			nettyResponse.setHeader(CONTENT_TYPE, "text/plain");
			nettyResponse.setHeader(CONTENT_LENGTH, bytes.length);

			String format = Request.current().format;
			if (format == null || ("XMLHttpRequest".equals(request.headers.get("x-requested-with")) && "html".equals(format))) {
				format = "txt";
			}

			ChannelBuffer buf = ChannelBuffers.copiedBuffer(bytes);
			nettyResponse.setContent(buf);
			ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
			if (!PlayHandler.isKeepAlive(nettyRequest)) { // keep alive will let
															// the
				// client hang, why? Content Length header!!
				writeFuture.addListener(ChannelFutureListener.CLOSE);
			}
		} catch (UnsupportedEncodingException fex) {
			Logger.error(fex, "(utf-8 ?)");
		}
	}

	@Override
	public void execute() throws Exception {
		if (!ctx.getChannel().isConnected()) {
			try {
				ctx.getChannel().close();
			} catch (Throwable e) {
				// Ignore
			}
			return;
		}
		ActionInvoker.invoke(request, response);
		PlayHandler.saveExceededSizeError(nettyRequest, request, response);
		PlayHandler.copyResponse(ctx, request, response, nettyRequest);
		Logger.trace("execute: end");
	}
}