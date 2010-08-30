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
import japidviews._javatags.*;
import static play.templates.JavaExtensions.*;
import static cn.bran.play.JapidPlayAdapter.*;
import static play.data.validation.Validation.*;
import static cn.bran.play.WebUtils.*;
// NOTE: This file was generated from: japidviews/Application/index.html
// Change to this file will be lost next time the template file is compiled.
@cn.bran.play.NoEnhance
public class index extends main{
	public static final String sourceTemplate = "japidviews/Application/index.html";
static private final String static_0 = ""
;
static private final String static_1 = ""
;
static private final String static_2 = ""
;
static private final String static_3 = "\n" + 
"<p id=\"time\">\n" + 
"    <span>"
;
static private final String static_4 = "</span>"
;
static private final String static_5 = "</p>\n" + 
"\n" + 
"<script type=\"text/javascript\" charset=\"utf-8\">\n" + 
"    setInterval(function() {\n" + 
"        $('section').load('"
;
static private final String static_6 = " #time')\n" + 
"    }, 1000)\n" + 
"</script>"
;
	public index() {
		super(null);
	}
	public index(StringBuilder out) {
		super(out);
	}
	Date now;
	public cn.bran.japid.template.RenderResult render(Date now) {
		this.now = now;
		long t = -1;
		super.layout();
		return new cn.bran.japid.template.RenderResultPartial(this.headers, getOut(), t, actionRunners);
	}
	@Override protected void doLayout() {
p(static_0);// line 1
p(static_1);// line 1
p(static_2);// line 2
// line 4
p(static_3);// line 4
p(format(now, "EEEE',' MMMM dd',' yyyy"));// line 7
p(static_4);// line 7
p(format(now, "hh'h' MM'mn' ss's'"));// line 7
p(static_5);// line 7
p(lookup("index", new Object[]{}));// line 12
p(static_6);// line 12

	}
	@Override protected void title() {
		p("Home");;
	}
}
