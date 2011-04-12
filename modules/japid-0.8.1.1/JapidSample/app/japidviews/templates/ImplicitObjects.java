package japidviews.templates;
import java.util.*;
import java.io.*;
import cn.bran.japid.tags.Each;
import static play.templates.JavaExtensions.*;
import static cn.bran.play.JapidPlayAdapter.*;
import static play.data.validation.Validation.*;
import japidviews._layouts.*;
import play.i18n.Messages;
import static  japidviews._javatags.JapidWebUtil.*;
import play.data.validation.Validation;
import play.mvc.Scope.*;
import models.*;
import play.data.validation.Error;
import play.i18n.Lang;
import japidviews._tags.*;
import controllers.*;
import play.mvc.Http.*;
import japidviews._javatags.*;
//
// NOTE: This file was generated from: japidviews/templates/ImplicitObjects.html
// Change to this file will be lost next time the template file is compiled.
//
@cn.bran.play.NoEnhance
public class ImplicitObjects extends cn.bran.japid.template.JapidTemplateBase
{	public static final String sourceTemplate = "japidviews/templates/ImplicitObjects.html";
{
	headers.put("Content-Type", "text/html; charset=utf-8");
}

// - add implicit fields with Play

	final Request request = Request.current(); 
	final Response response = Response.current(); 
	final Session session = Session.current();
	final RenderArgs renderArgs = RenderArgs.current();
	final Params params = Params.current();
	final Validation validation = Validation.current();
	final cn.bran.play.FieldErrors errors = new cn.bran.play.FieldErrors(validation);
	final play.Play _play = new play.Play(); 

// - end of implicit fields with Play 


	public ImplicitObjects() {
		super(null);
	}
	public ImplicitObjects(StringBuilder out) {
		super(out);
	}
	public cn.bran.japid.template.RenderResult render() {
		long t = -1;
		super.layout();
		return new cn.bran.japid.template.RenderResult(this.headers, getOut(), t);
	}
	@Override protected void doLayout() {
//------
p("\n" + 
"\n" + 
"<p>request: ");// line 1
p(request);// line 3
p("</p>\n" + 
"<p>response: ");// line 3
p(response);// line 4
p("</p>\n" + 
"<p>flash: ");// line 4
p(flash);// line 5
p("</p>\n" + 
"<p>errors: ");// line 5
p(errors);// line 6
p("</p>\n" + 
"<p>session: ");// line 6
p(session);// line 7
p("</p>\n" + 
"<p>renderArgs: ");// line 7
p(renderArgs);// line 8
p("</p>\n" + 
"<p>params: ");// line 8
p(params);// line 9
p("</p>\n" + 
"<p>validation: ");// line 9
p(validation);// line 10
p("</p>\n" + 
"<p>play: ");// line 10
p(_play);// line 11
p("</p>");// line 11

	}

}