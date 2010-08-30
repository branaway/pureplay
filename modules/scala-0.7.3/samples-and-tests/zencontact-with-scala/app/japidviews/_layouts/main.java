package japidviews._layouts;
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
import play.mvc.Http.Request;
import static play.templates.JavaExtensions.*;
import static cn.bran.play.JapidPlayAdapter.*;
import static play.data.validation.Validation.*;
import static cn.bran.play.WebUtils.*;
// NOTE: This file was generated from: japidviews/_layouts/main.html
// Change to this file will be lost next time the template file is compiled.
@cn.bran.play.NoEnhance
public abstract class main extends cn.bran.japid.template.JapidTemplateBase{
	public static final String sourceTemplate = "japidviews/_layouts/main.html";
static private final String static_0 = ""
;
static private final String static_1 = "<!DOCTYPE html>\n" + 
"<html>\n" + 
"    <head>\n" + 
"    	<title>Zencontact, by zenexity  ★ "
;
static private final String static_2 = "</title>\n" + 
"    	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" + 
"        <link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
;
static private final String static_3 = "\" />\n" + 
"        <link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
;
static private final String static_4 = "\" />\n" + 
"    	<script src=\""
;
static private final String static_5 = "\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" + 
"    	<script src=\""
;
static private final String static_6 = "\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" + 
"    	<script src=\""
;
static private final String static_7 = "\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" + 
"    </head>\n" + 
"	<body>\n" + 
"	    <div id=\"zencontact\">\n" + 
"    		<header>\n" + 
"    			<img src=\""
;
static private final String static_8 = "\" alt=\"logo\" id=\"logo\" />\n" + 
"    			<h1>Zencontact <span>by zenexity</span></h1>\n" + 
"    		</header>\n" + 
"    		<nav>\n" + 
"    			"
;
static private final String static_9 = "    			<a id=\"home\" href=\""
;
static private final String static_10 = "\" class='"
;
static private final String static_11 = "'>Home</a>\n" + 
"    			<a id=\"list\" href=\""
;
static private final String static_12 = "\" class='"
;
static private final String static_13 = "'>List</a>\n" + 
"    			<a id=\"new\" href=\""
;
static private final String static_14 = "\" class='"
;
static private final String static_15 = "'>New</a>\n" + 
"    		</nav>\n" + 
"    		<section>\n" + 
"    		    "
;
static private final String static_16 = "    		</section>\n" + 
"    		<footer>\n" + 
"    			<a href=\"http://www.w3.org/TR/html5/\">html5</a> - \n" + 
"    			<a href=\"http://www.w3.org/TR/css3-roadmap/\">css3</a> - \n" + 
"    			<a href=\"http://www.playframework.org/\">playframework</a> \n" + 
"    		</footer>\n" + 
"		</div>\n" + 
"	</body>\n" + 
"</html>"
;
	public main() {
		super(null);
	}
	public main(StringBuilder out) {
		super(out);
	}
	@Override public void layout() {		p(static_0);// line 1
p(static_1);// line 1
	title();// line 6
p(static_2);// line 6
p(lookupStatic("public/stylesheets/style.css"));// line 8
p(static_3);// line 8
p(lookupStatic("public/stylesheets/south-street/jquery-ui-1.7.2.custom.css"));// line 9
p(static_4);// line 9
p(lookupStatic("public/javascripts/jquery-1.4.min.js"));// line 10
p(static_5);// line 10
p(lookupStatic("public/javascripts/jquery-ui-1.7.2.custom.min.js"));// line 11
p(static_6);// line 11
p(lookupStatic("public/javascripts/jquery.editinplace.packed.js"));// line 12
p(static_7);// line 12
p(lookupStatic("public/images/logo.png"));// line 17
p(static_8);// line 17
Request request = Request.current();// line 21
p(static_9);// line 21
p(lookup("index", new Object[]{}));// line 22
p(static_10);// line 22
p(request.action.startsWith("/index/") ? "selected" : "");// line 22
p(static_11);// line 22
p(lookup("list", new Object[]{}));// line 23
p(static_12);// line 23
p(request.action.startsWith("/list/") ? "selected" : "");// line 23
p(static_13);// line 23
p(lookup("form", new Object[]{}));// line 24
p(static_14);// line 24
p(request.action.startsWith("/form/") || request.action.startsWith("/save/") ? "selected" : "");// line 24
p(static_15);// line 24
	doLayout();// line 27
p(static_16);// line 27
	}	protected abstract void title();
	protected abstract void doLayout();
}