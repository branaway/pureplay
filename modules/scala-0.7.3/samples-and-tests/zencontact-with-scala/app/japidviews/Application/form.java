package japidviews.Application;
import java.util.*;
import java.io.*;
import cn.bran.japid.tags.Each;
import japidviews._layouts.*;
import static  japidviews._javatags.JapidWebUtil.*;
import play.data.validation.Validation;
import play.mvc.Scope.*;
import models.*;
import play.data.validation.Error;
import japidviews._tags.*;
import controllers.*;
import play.mvc.Http.*;
import japidviews._javatags.*;
import static play.templates.JavaExtensions.*;
import static cn.bran.play.JapidPlayAdapter.*;
import static play.data.validation.Validation.*;
import static cn.bran.play.WebUtils.*;
// NOTE: This file was generated from: japidviews/Application/form.html
// Change to this file will be lost next time the template file is compiled.
@cn.bran.play.NoEnhance
public class form extends main{
	public static final String sourceTemplate = "japidviews/Application/form.html";
static private final String static_0 = ""
;
static private final String static_1 = ""
;
static private final String static_2 = ""
;
static private final String static_3 = "\n" + 
"<form action=\""
;
static private final String static_4 = "\">\n" + 
"    <input type=\"hidden\" name=\"contact.id\" value=\""
;
static private final String static_5 = "\">\n" + 
"    \n" + 
"    <p class=\"field\">\n" + 
"        <label for=\"name\">Name:</label>\n" + 
"        <input type=\"text\" id=\"name\" name=\"contact.name\" value=\""
;
static private final String static_6 = "\">\n" + 
"        <span class=\"error\">"
;
static private final String static_7 = "</span>\n" + 
"    </p>\n" + 
"\n" + 
"    <p class=\"field\">\n" + 
"        <label for=\"firstname\">First name:</label>\n" + 
"        <input type=\"text\" id=\"firstname\" name=\"contact.firstname\" value=\""
;
static private final String static_8 = "\">\n" + 
"        <span class=\"error\">"
;
static private final String static_9 = "</span>\n" + 
"    </p>\n" + 
"\n" + 
"    <p class=\"field\">\n" + 
"        <label for=\"birthdate\">Birth date:</label>\n" + 
"        <input type=\"text\" id=\"birthdate\" name=\"contact.birthdate\" value=\""
;
static private final String static_10 = "\">\n" + 
"        <span class=\"error\">"
;
static private final String static_11 = "</span>\n" + 
"    </p>\n" + 
"\n" + 
"    <p class=\"field\">\n" + 
"        <label for=\"email\">Email:</label>\n" + 
"        <input type=\"text\" id=\"email\" name=\"contact.email\" value=\""
;
static private final String static_12 = "\">\n" + 
"        <span class=\"error\">"
;
static private final String static_13 = "</span>\n" + 
"    </p>\n" + 
"\n" + 
"    <p class=\"buttons\">\n" + 
"     	<a href=\""
;
static private final String static_14 = "\">Cancel</a> or <input type=\"submit\" value=\"Save this contact\" id=\"saveContact\">\n" + 
"    </p>\n" + 
"    \n" + 
"    <script type=\"text/javascript\" charset=\"utf-8\">\n" + 
"        $(\"#birthdate\").datepicker({dateFormat:'yy-mm-dd', showAnim:'fadeIn'})\n" + 
"	</script>\n" + 
"</form>\n" + 
"";
	public form() {
		super(null);
	}
	public form(StringBuilder out) {
		super(out);
	}
	Contact contact;
	public cn.bran.japid.template.RenderResult render(Contact contact) {
		this.contact = contact;
		long t = -1;
		super.layout();
		return new cn.bran.japid.template.RenderResultPartial(this.headers, getOut(), t, actionRunners);
	}
	@Override protected void doLayout() {
		play.mvc.Http.Request request = play.mvc.Http.Request.current();
		play.mvc.Http.Response response = play.mvc.Http.Response.current();
		play.mvc.Scope.Flash flash = play.mvc.Scope.Flash.current();
		play.mvc.Scope.Session session = play.mvc.Scope.Session.current();
		play.mvc.Scope.RenderArgs renderArgs = play.mvc.Scope.RenderArgs.current();
		play.mvc.Scope.Params params = play.mvc.Scope.Params.current();
		play.data.validation.Validation validation = play.data.validation.Validation.current();
		cn.bran.play.FieldErrors errors = new cn.bran.play.FieldErrors(validation.errors());
p(static_0);// line 1
// line 1
p(static_1);// line 1
p(static_2);// line 2
// line 4
p(static_3);// line 4
p(lookup("save", new Object[]{}));// line 6
p(static_4);// line 6
p(contact != null ? contact.id : "");// line 7
p(static_5);// line 7
p(contact != null ? contact.name : "");// line 11
p(static_6);// line 11
p(errors.forKey("contact.name"));// line 12
p(static_7);// line 12
p(contact  != null ? contact.firstname :"");// line 17
p(static_8);// line 17
p(errors.forKey("contact.firstname"));// line 18
p(static_9);// line 18
p(contact != null ? (contact.birthdate != null? format(contact.birthdate, "yyyy-MM-dd") : "") : "");// line 23
p(static_10);// line 23
p(errors.forKey("contact.birthdate"));// line 24
p(static_11);// line 24
p(contact != null ? contact.email :"");// line 29
p(static_12);// line 29
p(errors.forKey("contact.email"));// line 30
p(static_13);// line 30
p(lookup("list", new Object[]{}));// line 34
p(static_14);// line 34

	}
	@Override protected void title() {
		p("Form");;
	}
}
