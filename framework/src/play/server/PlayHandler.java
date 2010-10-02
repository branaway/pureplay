package play.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ETAG;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.IF_NONE_MATCH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SERVER;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.handler.stream.ChunkedStream;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.data.validation.Validation;
import play.exceptions.PlayException;
import play.exceptions.UnexpectedException;
import play.i18n.Messages;
import play.libs.MimeTypes;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope;
import play.mvc.results.NotFound;
import play.templates.JavaExtensions;
import play.utils.Utils;
import play.vfs.VirtualFile;
import bran.RestartException;

public class PlayHandler extends SimpleChannelUpstreamHandler {

	private final static String signature = "PurePlay!;" + Play.version + ";" + Play.mode.name().toLowerCase();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Logger.trace("messageReceived: begin");

		final Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			final HttpRequest nettyRequest = (HttpRequest) msg;
			final Request request = parseRequest(ctx, nettyRequest);
			final Response response = new Response();

			try {

				Http.Response.current.set(response);
				response.out = new ByteArrayOutputStream(1000); 
//				response.out = BaosThreadStore.checkout();   // this is slower than plain new! wired...
				boolean raw = false;
				for (PlayPlugin plugin : Play.plugins) {
					if (plugin.rawInvocation(request, response)) {
						raw = true;
						break;
					}
				}
				if (raw) {
					copyResponse(ctx, request, response, nettyRequest);
				} else {
//					Invoker.invoke(new NettyInvocation(request, response, ctx, nettyRequest, e));
					// direct invocation is faster, but suspend won't work.
					NettyInvocation nettyInvocation = new NettyInvocation(request, response, ctx, nettyRequest, e);
					// bran hack to implement two phase change detection
					try {
						nettyInvocation.run();
					}
					catch (RestartException re) {
						Play.start();
						nettyInvocation.run();
					}
				}

			} 
			catch (NotFound ex) {
				// bran: the suspend mechanism can be moved here if w use direct NettyInvocation
				serve404(ex, ctx, request, nettyRequest);
			}
			catch (Exception ex) {
				// bran: the suspend mechanism can be moved here if w use direct NettyInvocation
				serve500(ex, ctx, nettyRequest);
			}
		}
		Logger.trace("messageReceived: end");
	}


	static void saveExceededSizeError(HttpRequest nettyRequest, Request request, Response response) {

		String warning = nettyRequest.getHeader(HttpHeaders.Names.WARNING);
		String length = nettyRequest.getHeader(HttpHeaders.Names.CONTENT_LENGTH);
		if (warning != null) {
			try {
				StringBuilder error = new StringBuilder();
				error.append("\u0000");
				// Cannot put warning which is
				// play.netty.content.length.exceeded
				// as Key as it will result error when printing error
				error.append("play.netty.maxContentLength");
				error.append(":");
				String size = null;
				try {
					size = JavaExtensions.formatSize(Long.parseLong(length));
				} catch (Exception e) {
					size = length + " bytes";
				}
				error.append(Messages.get(warning, size));
				error.append("\u0001");
				error.append(size);
				error.append("\u0000");
				if (request.cookies.get(Scope.COOKIE_PREFIX + "_ERRORS") != null
						&& request.cookies.get(Scope.COOKIE_PREFIX + "_ERRORS").value != null) {
					error.append(request.cookies.get(Scope.COOKIE_PREFIX + "_ERRORS").value);
				}
				String errorData = URLEncoder.encode(error.toString(), "utf-8");
				Http.Cookie c = new Http.Cookie();
				c.value = errorData;
				c.name = Scope.COOKIE_PREFIX + "_ERRORS";
				request.cookies.put(Scope.COOKIE_PREFIX + "_ERRORS", c);
			} catch (Exception e) {
				throw new UnexpectedException("Error serialization problem", e);
			}
		}
	}

	/**
	 * bran: http 1.1 defaults to keep-alive, but 1.0 has no spec about it. Some browsers will observe 
	 * the Connection: Keep-Alive header. 
	 * 
	 * @param nettyResponse
	 */
	static public void addKeepAliveHeader(HttpResponse nettyResponse) {
		nettyResponse.setHeader("Connection", "Keep-Alive");
	}
	
	protected static void addToResponse(Response response, HttpResponse nettyResponse) {
		Map<String, Http.Header> headers = response.headers;
		for (Map.Entry<String, Http.Header> entry : headers.entrySet()) {
			Http.Header hd = entry.getValue();
			for (String value : hd.values) {
				nettyResponse.setHeader(entry.getKey(), value);
			}
		}
		Map<String, Http.Cookie> cookies = response.cookies;

		for (Http.Cookie cookie : cookies.values()) {
			CookieEncoder encoder = new CookieEncoder(true);
			Cookie c = new DefaultCookie(cookie.name, cookie.value);
			c.setSecure(cookie.secure);
			c.setPath(cookie.path);
			if (cookie.domain != null) {
				c.setDomain(cookie.domain);
			}
			if (cookie.maxAge != null) {
				c.setMaxAge(cookie.maxAge);
			}
			encoder.addCookie(c);
			nettyResponse.addHeader(SET_COOKIE, encoder.encode());
		}

		if (!response.headers.containsKey(CACHE_CONTROL)) {
			nettyResponse.setHeader(CACHE_CONTROL, "no-cache");
		}

	}

	protected static void writeResponse(ChannelHandlerContext ctx, Response response, HttpResponse nettyResponse, HttpRequest nettyRequest) {
		Logger.trace("writeResponse: begin");
		byte[] content = null;

		final boolean keepAlive = isKeepAlive(nettyRequest);
		if (keepAlive)
			PlayHandler.addKeepAliveHeader(nettyResponse);

		if (nettyRequest.getMethod().equals(HttpMethod.HEAD)) {
			content = new byte[0];
		} else {
			content = response.out.toByteArray();
		}

		ChannelBuffer buf = ChannelBuffers.copiedBuffer(content);
		nettyResponse.setContent(buf);

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			Logger.trace("writeResponse: content length [" + response.out.size() + "]");
			setContentLength(nettyResponse, response.out.size());
		}

		ChannelFuture f = ctx.getChannel().write(nettyResponse);

		// Decide whether to close the connection or not.
		if (!keepAlive) {
			// Close the connection when the whole content is written out.
			f.addListener(ChannelFutureListener.CLOSE);
		}
		Logger.trace("writeResponse: end");
	}

	public static void copyResponse(ChannelHandlerContext ctx, Request request, Response response, HttpRequest nettyRequest)
			throws Exception {
		Logger.trace("copyResponse: begin");
		// response.out.flush();

		// Decide whether to close the connection or not.
		// bran: match the version
		HttpVersion protocolVersion = nettyRequest.getProtocolVersion();
		HttpResponse nettyResponse = new DefaultHttpResponse(protocolVersion, HttpResponseStatus.valueOf(response.status));
		nettyResponse.setHeader(SERVER, signature);

		if (response.contentType != null) {
			nettyResponse.setHeader(CONTENT_TYPE,
					response.contentType
							+ (response.contentType.startsWith("text/") && !response.contentType.contains("charset") ? "; charset=utf-8"
									: ""));
		} else {
			nettyResponse.setHeader(CONTENT_TYPE, "text/plain; charset=utf-8");
		}

		addToResponse(response, nettyResponse);

		final Object obj = response.direct;
		File file = null;
		InputStream is = null;
		if (obj instanceof File) {
			file = (File) obj;
		} else if (obj instanceof InputStream) {
			is = (InputStream) obj;
		}

		final boolean keepAlive = isKeepAlive(nettyRequest);
		if (keepAlive)
			addKeepAliveHeader(nettyResponse);

		if (file != null && file.isFile()) {
			try {
				nettyResponse = addEtag(nettyRequest, nettyResponse, file);
				if (nettyResponse.getStatus().equals(HttpResponseStatus.NOT_MODIFIED)) {

					Channel ch = ctx.getChannel();

					// Write the initial line and the header.
					ChannelFuture writeFuture = ch.write(nettyResponse);

					if (!keepAlive) {
						// Close the connection when the whole content is
						// written out.
						writeFuture.addListener(ChannelFutureListener.CLOSE);
					}
				} else {
					nettyResponse.setHeader(CONTENT_TYPE, MimeTypes.getContentType(file.getName(), "text/plain"));
					RandomAccessFile raf = new RandomAccessFile(file, "r");
					long fileLength = raf.length();

					if (keepAlive) {
						// Add 'Content-Length' header only for a keep-alive
						// connection.
						Logger.trace("file length is [" + fileLength + "]");
						setContentLength(nettyResponse, fileLength);
					}

					Channel ch = ctx.getChannel();

					// Write the initial line and the header.
					ChannelFuture writeFuture = ch.write(nettyResponse);

					// Write the content.
					// If it is not a HEAD
					if (!nettyRequest.getMethod().equals(HttpMethod.HEAD)) {
						writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
					}
					if (!keepAlive) {
						// Close the connection when the whole content is
						// written out.
						writeFuture.addListener(ChannelFutureListener.CLOSE);
					}

				}
			} catch (Exception e) {
				throw e;
			}
		} else if (is != null) {
			ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
			if (!nettyRequest.getMethod().equals(HttpMethod.HEAD) && !nettyResponse.getStatus().equals(HttpResponseStatus.NOT_MODIFIED)) {
				writeFuture = ctx.getChannel().write(new ChunkedStream(is));
			}
			if (!keepAlive) {
				writeFuture.addListener(ChannelFutureListener.CLOSE);
			}
		} else {
			writeResponse(ctx, response, nettyResponse, nettyRequest);
		}
		Logger.trace("copyResponse: end");
	}

	public static Request parseRequest(ChannelHandlerContext ctx, HttpRequest nettyRequest) throws Exception {
		Logger.trace("parseRequest: begin");
		Logger.trace("parseRequest: URI = " + nettyRequest.getUri());
		int index = nettyRequest.getUri().indexOf("?");
		String querystring = "";
		String path = URLDecoder.decode(nettyRequest.getUri(), "UTF-8");
		if (index != -1) {
			path = URLDecoder.decode(nettyRequest.getUri().substring(0, index), "UTF-8");
			querystring = nettyRequest.getUri().substring(index + 1);
		}

		final Request request = new Request();
		request.remoteAddress = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
		request.method = nettyRequest.getMethod().getName();
		request.path = path;
		request.querystring = querystring;
		final String contentType = nettyRequest.getHeader(CONTENT_TYPE);
		if (contentType != null) {
			request.contentType = contentType.split(";")[0].trim().toLowerCase();
		} else {
			request.contentType = "text/html";
		}

		if (nettyRequest.getHeader("X-HTTP-Method-Override") != null) {
			request.method = nettyRequest.getHeader("X-HTTP-Method-Override").intern();
		}

		ChannelBuffer b = nettyRequest.getContent();
		if (b instanceof FileChannelBuffer) {
			FileChannelBuffer buffer = (FileChannelBuffer) b;
			// An error occurred
			Integer max = Integer.valueOf(Play.configuration.getProperty("play.netty.maxContentLength", "-1"));

			request.body = buffer.getInputStream();
			if (!(max == -1 || request.body.available() < max)) {
				request.body = new ByteArrayInputStream(new byte[0]);
			}

		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(new ChannelBufferInputStream(b), out);
			byte[] n = out.toByteArray();
			request.body = new ByteArrayInputStream(n);
		}

		request.url = nettyRequest.getUri();
		request.host = nettyRequest.getHeader(HOST);

		if (request.host == null) {
			request.host = "";
			request.port = 80;
			request.domain = "";
		} else {
			if (request.host.contains(":")) {
				final String[] host = request.host.split(":");
				request.port = Integer.parseInt(host[1]);
				request.domain = host[0];
			} else {
				request.port = 80;
				request.domain = request.host;
			}
		}

		if (Play.configuration.containsKey("XForwardedSupport") && nettyRequest.getHeader("X-Forwarded-For") != null) {
			if (!Arrays.asList(Play.configuration.getProperty("XForwardedSupport", "127.0.0.1").split(",")).contains(request.remoteAddress)) {
				throw new RuntimeException("This proxy request is not authorized: " + request.remoteAddress);
			} else {
				request.secure = ("https".equals(Play.configuration.get("XForwardedProto"))
						|| "https".equals(nettyRequest.getHeader("X-Forwarded-Proto")) || "on".equals(nettyRequest
						.getHeader("X-Forwarded-Ssl")));
				if (Play.configuration.containsKey("XForwardedHost")) {
					request.host = (String) Play.configuration.get("XForwardedHost");
				} else if (nettyRequest.getHeader("X-Forwarded-Host") != null) {
					request.host = nettyRequest.getHeader("X-Forwarded-Host");
				}
				if (nettyRequest.getHeader("X-Forwarded-For") != null) {
					request.remoteAddress = nettyRequest.getHeader("X-Forwarded-For");
				}
			}
		}

		addToRequest(nettyRequest, request);

		request.resolveFormat();

		request._init();

		Logger.trace("parseRequest: end");
		return request;
	}

	protected static void addToRequest(HttpRequest nettyRequest, Request request) {
		for (String key : nettyRequest.getHeaderNames()) {
			Http.Header hd = new Http.Header();
			hd.name = key.toLowerCase();
			hd.values = new ArrayList<String>();
			for (String next : nettyRequest.getHeaders(key)) {
				hd.values.add(next);
			}
			request.headers.put(hd.name, hd);
		}

		String value = nettyRequest.getHeader(COOKIE);
		if (value != null) {
			Set<Cookie> cookies = new CookieDecoder().decode(value);
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					Http.Cookie playCookie = new Http.Cookie();
					playCookie.name = cookie.getName();
					playCookie.path = cookie.getPath();
					playCookie.domain = cookie.getDomain();
					playCookie.secure = cookie.isSecure();
					playCookie.value = cookie.getValue();
					request.cookies.put(playCookie.name, playCookie);
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// e.getCause().printStackTrace();
		e.getChannel().close();
	}

	public static void serve404(NotFound e, ChannelHandlerContext ctx, Request request, HttpRequest nettyRequest) {
		Logger.trace("serve404: begin");
		HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		nettyResponse.setHeader(SERVER, signature);

		nettyResponse.setHeader(CONTENT_TYPE, "text/html");
		Map<String, Object> binding = getBindingForErrors(e, false);

		String format = Request.current().format;
		if (format == null || ("XMLHttpRequest".equals(request.headers.get("x-requested-with")) && "html".equals(format))) {
			format = "txt";
		}
		nettyResponse.setHeader(CONTENT_TYPE, (MimeTypes.getContentType("404." + format, "text/plain")));

		// String errorHtml = TemplateLoader.load("errors/404." +
		// format).render(binding);
		// bran
		String errorHtml = "404: url not found. -- bran";
		try {
			ChannelBuffer buf = ChannelBuffers.copiedBuffer(errorHtml.getBytes("utf-8"));
			nettyResponse.setContent(buf);
			ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		} catch (UnsupportedEncodingException fex) {
			Logger.error(fex, "(utf-8 ?)");
		}
		Logger.trace("serve404: end");
	}

	protected static Map<String, Object> getBindingForErrors(Exception e, boolean isError) {

		Map<String, Object> binding = new HashMap<String, Object>();
		if (!isError) {
			binding.put("result", e);
		} else {
			binding.put("exception", e);
		}
		binding.put("session", Scope.Session.current());
		binding.put("request", Http.Request.current());
		binding.put("flash", Scope.Flash.current());
		binding.put("params", Scope.Params.current());
		binding.put("play", new Play());
		try {
			binding.put("errors", Validation.errors());
		} catch (Exception ex) {
			// Logger.error(ex, "Error when getting Validation errors");
		}

		return binding;
	}

	// TODO: add request and response as parameter
	public static void serve500(Exception e, ChannelHandlerContext ctx, HttpRequest nettyRequest) {
		Logger.trace("serve500: begin");
		HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		nettyResponse.setHeader(SERVER, signature);

		Request request = Request.current();
		Response response = Response.current();

		try {
			if (!(e instanceof PlayException)) {
				e = new play.exceptions.UnexpectedException(e);
			}

			// Flush some cookies
			try {

				Map<String, Http.Cookie> cookies = response.cookies;
				for (Http.Cookie cookie : cookies.values()) {
					CookieEncoder encoder = new CookieEncoder(true);
					Cookie c = new DefaultCookie(cookie.name, cookie.value);
					c.setSecure(cookie.secure);
					c.setPath(cookie.path);
					if (cookie.domain != null) {
						c.setDomain(cookie.domain);
					}
					if (cookie.maxAge != null) {
						c.setMaxAge(cookie.maxAge);
					}
					encoder.addCookie(c);
					nettyResponse.addHeader(SET_COOKIE, encoder.encode());
					// nettyResponse.setHeader("Cookie", encoder.encode());
				}

			} catch (Exception exx) {
				Logger.error(e, "Trying to flush cookies");
				// humm ?
			}
			Map<String, Object> binding = getBindingForErrors(e, true);

			String format = request.format;
			if (format == null || ("XMLHttpRequest".equals(request.headers.get("x-requested-with")) && "html".equals(format))) {
				format = "txt";
			}

			nettyResponse.setHeader("Content-Type", (MimeTypes.getContentType("500." + format, "text/plain")));
			try {
				// String errorHtml = TemplateLoader.load("errors/500." +
				// format).render(binding);
				// bran, very simple error for now
				String errorHtml = "Error 500: ";
				if (e instanceof UnexpectedException) {
					// errorHtml +=
					// ((UnexpectedException)e).getCause().getMessage();
					errorHtml += ((UnexpectedException) e).getErrorDescription();
				} else {
					errorHtml += e.getMessage();
				}

				ChannelBuffer buf = ChannelBuffers.copiedBuffer(errorHtml.getBytes("utf-8"));
				nettyResponse.setContent(buf);
				ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
				writeFuture.addListener(ChannelFutureListener.CLOSE);
				Logger.error(e, "Internal Server Error (500) for request %s", request.method + " " + request.url);
			} catch (Throwable ex) {
				Logger.error(e, "Internal Server Error (500) for request %s", request.method + " " + request.url);
				Logger.error(ex, "Error during the 500 response generation");
				try {
					ChannelBuffer buf = ChannelBuffers.copiedBuffer("Internal Error (check logs)".getBytes("utf-8"));
					nettyResponse.setContent(buf);
					ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
					writeFuture.addListener(ChannelFutureListener.CLOSE);
				} catch (UnsupportedEncodingException fex) {
					Logger.error(fex, "(utf-8 ?)");
				}
			}
		} catch (Throwable exxx) {
			try {
				ChannelBuffer buf = ChannelBuffers.copiedBuffer("Internal Error (check logs)".getBytes("utf-8"));
				nettyResponse.setContent(buf);
				ChannelFuture writeFuture = ctx.getChannel().write(nettyResponse);
				writeFuture.addListener(ChannelFutureListener.CLOSE);
			} catch (Exception fex) {
				Logger.error(fex, "(utf-8 ?)");
			}
			if (exxx instanceof RuntimeException) {
				throw (RuntimeException) exxx;
			}
			throw new RuntimeException(exxx);
		}
		Logger.trace("serve500: end");
	}

	public static void serveStatic(String staticFile, ChannelHandlerContext ctx, Request request, Response response, HttpRequest nettyRequest) {
		Logger.trace("serveStatic: begin");
		HttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.status));
		nettyResponse.setHeader("Server", signature);
		try {
			VirtualFile file = Play.getVirtualFile(staticFile);
			if (file != null && file.exists() && file.isDirectory()) {
				// allow directory default
				file = file.child("index.html");
				if (file != null) {
					staticFile = file.relativePath();
				}
			}
			if ((file == null || !file.exists())) {
				serve404(new NotFound("The file " + staticFile + " does not exist"), ctx, request, nettyRequest);
			} else {
				boolean raw = false;
				for (PlayPlugin plugin : Play.plugins) {
					if (plugin.serveStatic(file, Request.current(), Response.current())) {
						raw = true;
						break;
					}
				}
				if (raw) {
					copyResponse(ctx, request, response, nettyRequest);
				} else {
					final File localFile = file.getRealFile();
					final boolean keepAlive = isKeepAlive(nettyRequest);
					if (keepAlive)
						PlayHandler.addKeepAliveHeader(nettyResponse);

					nettyResponse = addEtag(nettyRequest, nettyResponse, localFile);

					if (nettyResponse.getStatus().equals(HttpResponseStatus.NOT_MODIFIED)) {

//						Channel ch = e.getChannel();
						// bran will ctx have everything?
						Channel ch = ctx.getChannel();
						

						// Write the initial line and the header.
						ChannelFuture writeFuture = ch.write(nettyResponse);
						if (!keepAlive) {
							// Write the content.
							writeFuture.addListener(ChannelFutureListener.CLOSE);
						}
					} else {

						RandomAccessFile raf;
						raf = new RandomAccessFile(localFile, "r");
						long fileLength = raf.length();

						Logger.trace("keep alive " + keepAlive);
						Logger.trace("content type " + (MimeTypes.getContentType(localFile.getName(), "text/plain")));

						if (keepAlive && !nettyResponse.getStatus().equals(HttpResponseStatus.NOT_MODIFIED)) {
							// Add 'Content-Length' header only for a keep-alive
							// connection.
							Logger.trace("file length " + fileLength);
							setContentLength(nettyResponse, fileLength);
						}

						nettyResponse.setHeader(CONTENT_TYPE, (MimeTypes.getContentType(localFile.getName(), "text/plain")));

//						Channel ch = e.getChannel();
						Channel ch = ctx.getChannel();

						// Write the initial line and the header.
						ch.write(nettyResponse);

						// bran modified to use zero-copy for http. No,zero-copy on windows is slower!
						ChannelFuture writeFuture;
						boolean dontUseZeroCopy = true;
				        if (dontUseZeroCopy || ch.getPipeline().get(SslHandler.class) != null) {
				            // Cannot use zero-copy with HTTPS.
				            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
				        } else {
				            // No encryption - use zero-copy.
				            final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
				            writeFuture = ch.write(region);
				            writeFuture.addListener(new ChannelFutureProgressListener() {
				                @Override
								public void operationComplete(ChannelFuture future) {
				                    region.releaseExternalResources();
				                }

								@Override
								public void operationProgressed(ChannelFuture arg0, long arg1, long arg2, long arg3) throws Exception {
								}
				            });
				        }

						if (!keepAlive) {
							// Close the connection when the whole content is
							// written out.
							writeFuture.addListener(ChannelFutureListener.CLOSE);
						}
					}
				}

			}
		} catch (Exception ez) {
			Logger.error(ez, "serveStatic for request %s", request.method + " " + request.url);
			try {
				ChannelBuffer buf = ChannelBuffers.copiedBuffer("Internal Error (check logs)".getBytes("utf-8"));
				nettyResponse.setContent(buf);
				ChannelFuture future = ctx.getChannel().write(nettyResponse);
				future.addListener(ChannelFutureListener.CLOSE);
			} catch (Exception ex) {
				Logger.error(ez, "serveStatic for request %s", request.method + " " + request.url);
			}
		}
		Logger.trace("serveStatic: end");
	}

	public static boolean isModified(String etag, long last, HttpRequest nettyRequest) {

		if (nettyRequest.containsHeader(IF_NONE_MATCH)) {
			final String browserEtag = nettyRequest.getHeader(IF_NONE_MATCH);
			if (browserEtag.equals(etag)) {
				return false;
			}
			return true;
		}

		if (nettyRequest.containsHeader(IF_MODIFIED_SINCE)) {
			final String ifModifiedSince = nettyRequest.getHeader(IF_MODIFIED_SINCE);

			if (!StringUtils.isEmpty(ifModifiedSince)) {
				try {
					Date browserDate = Utils.getHttpDateFormatter().parse(ifModifiedSince);
					if (browserDate.getTime() >= last) {
						return false;
					}
				} catch (ParseException ex) {
					Logger.warn("Can't parse HTTP date", ex);
				}
				return true;
			}
		}
		return true;
	}

	private static HttpResponse addEtag(HttpRequest nettyRequest, HttpResponse httpResponse, File file) {
		if (Play.mode == Play.Mode.DEV) {
			httpResponse.setHeader(CACHE_CONTROL, "no-cache");
		} else {
			String maxAge = Play.configuration.getProperty("http.cacheControl", "3600");
			if (maxAge.equals("0")) {
				httpResponse.setHeader(CACHE_CONTROL, "no-cache");
			} else {
				httpResponse.setHeader(CACHE_CONTROL, "max-age=" + maxAge);
			}
		}
		boolean useEtag = Play.configuration.getProperty("http.useETag", "true").equals("true");
		long last = file.lastModified();
		final String etag = "\"" + last + "-" + file.hashCode() + "\"";
		if (!isModified(etag, last, nettyRequest)) {
			if (nettyRequest.getMethod().equals(HttpMethod.GET)) {
				httpResponse.setStatus(HttpResponseStatus.NOT_MODIFIED);
			}
			if (useEtag) {
				httpResponse.setHeader(ETAG, etag);
			}

		} else {
			httpResponse.setHeader(LAST_MODIFIED, Utils.getHttpDateFormatter().format(new Date(last)));
			if (useEtag) {
				httpResponse.setHeader(ETAG, etag);
			}
		}
		return httpResponse;
	}

	public static boolean isKeepAlive(HttpMessage message) {
		return HttpHeaders.isKeepAlive(message) /*&& message.getProtocolVersion().equals(HttpVersion.HTTP_1_1)*/;
	}

	public static void setContentLength(HttpMessage message, long contentLength) {
		message.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(contentLength));
	}
}
