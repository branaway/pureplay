package bran;
// NOTE: This file was generated from: japidviews/Application/index.html
// Change to this file will be lost next time the template file is compiled.
@cn.bran.play.NoEnhance
public class index extends cn.bran.japid.template.JapidTemplateBase{
	public static final String sourceTemplate = "japidviews/Application/index.html";
static private final String static_0 = ""
;
static private final String static_1 = "Hello "
;
static private final String static_2 = "!\n" + 
"\n" + 
"<p><a href=\"/_ping\">ping the low level netty handler, the fastest</a>\n" + 
"<p><a href=\"/_netty\">return at the NettyInvocation level, fast</a>\n" + 
"<p><a href=\"/_japidDirect\">invoke a japid render process without the normal action invocation</a>\n" + 
"<p><a href=\"/PubSubController/pub?from=you&topic=hello&content=HowAreYouDoing\">pub a message to queue \"hello\"</a></p>\n" + 
"<p><a href=\"/PubSubController/sub?sid=123&topic=hello\">sub a topic \"hello\"</a></p>\n" + 
"\n" + 
"";
	public index() {
		super(null);
	}
	public index(StringBuilder out) {
		super(out);
	}
	String who;
	public cn.bran.japid.template.RenderResult render(String who) {
		this.who = who;
		long t = -1;
		super.layout();
		return new cn.bran.japid.template.RenderResultPartial(this.headers, getOut(), t, actionRunners);
	}
	@Override protected void doLayout() {

//		play.mvc.Http.Request request = play.mvc.Http.Request.current(); assert request != null;
//		play.mvc.Http.Response response = play.mvc.Http.Response.current(); assert response != null;
//		play.mvc.Scope.Flash flash = play.mvc.Scope.Flash.current();assert flash != null;
//		play.mvc.Scope.Session session = play.mvc.Scope.Session.current();assert session != null;
//		play.mvc.Scope.RenderArgs renderArgs = play.mvc.Scope.RenderArgs.current(); assert renderArgs != null;
//		play.mvc.Scope.Params params = play.mvc.Scope.Params.current();assert params != null;
//		play.data.validation.Validation validation = play.data.validation.Validation.current();assert validation!= null;
//		cn.bran.play.FieldErrors errors = new cn.bran.play.FieldErrors(validation.errors());assert errors != null;
//		play.Play _play = new play.Play(); assert _play != null;
p(static_0);// line 1
p(static_1);// line 1
p(who);// line 2
p(static_2);// line 2

	}
}
