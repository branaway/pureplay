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
// NOTE: This file was generated from: japidviews/templates/dumpPost.html
// Change to this file will be lost next time the template file is compiled.
//
@cn.bran.play.NoEnhance
public class dumpPost extends cn.bran.japid.template.JapidTemplateBase
{	public static final String sourceTemplate = "japidviews/templates/dumpPost.html";
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


	public dumpPost() {
		super(null);
	}
	public dumpPost(StringBuilder out) {
		super(out);
	}
	private String f1;
	private String f2;
	private String body;
	public cn.bran.japid.template.RenderResult render(String f1, String f2, String body) {
		this.f1 = f1;
		this.f2 = f2;
		this.body = body;
		long t = -1;
		super.layout();
		return new cn.bran.japid.template.RenderResult(this.headers, getOut(), t);
	}
	@Override protected void doLayout() {
//------
p("\n");// line 1
p("\n" + 
"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + 
"<html>\n" + 
"<head>\n" + 
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
"<title>Insert title here</title>\n" + 
"</head>\n" + 
"<body>\n" + 
"<form method=\"POST\" action=\"/Application/dumpPost\">\n" + 
"	<input type=\"text\" width=\"30\" name=\"f1\" value=\"");// line 3
try { p(f1); } catch (NullPointerException npe) {}// line 13
p("\"/>\n" + 
"	<input type=\"text\" width=\"30\" name=\"f2\" value=\"");// line 13
try { p(f2); } catch (NullPointerException npe) {}// line 14
p("\"/>\n" + 
"	<input type=\"text\" width=\"50\" name=\"body\" value=\"");// line 14
try { p(body); } catch (NullPointerException npe) {}// line 15
p("\"/>\n" + 
"	<input type=\"submit\"/>\n" + 
"</form>\n" + 
"\n" + 
"</body>\n" + 
"</html>");// line 15

	}

}