package japidviews.Application;
import static cn.bran.play.JapidPlayAdapter.lookup;
import static cn.bran.play.JapidPlayAdapter.lookupStatic;
import static japidviews._javatags.JapidWebUtil.jsAction;
import static play.templates.JavaExtensions.format;
import japidviews._layouts.main;

import java.util.List;

import models.Contact;
import play.Play;
import play.mvc.Scope.RenderArgs;
import cn.bran.japid.tags.Each;
// NOTE: This file was generated from: japidviews/Application/list.html
// Change to this file will be lost next time the template file is compiled.
@cn.bran.play.NoEnhance
public class list extends main{
	public static final String sourceTemplate = "japidviews/Application/list.html";
static private final String static_0 = ""
;
static private final String static_1 = ""
;
static private final String static_2 = ""
;
static private final String static_3 = "\n" + 
"<table>\n" + 
"	<thead>\n" + 
"		<tr>\n" + 
"			<th class=\"name\">Name</th>\n" + 
"			<th class=\"firstname\">First name</th>\n" + 
"			<th class=\"birthdate\">Birth date</th>\n" + 
"			<th class=\"email\">Email</th>\n" + 
"			<th class=\"edit\"></th>\n" + 
"		</tr>\n" + 
"	</thead>\n" + 
"	<tbody>\n" + 
"	    "
;
static private final String static_4 = "	    <tr class=\"contact\" contactId=\""
;
static private final String static_5 = "\" draggable=\"true\">\n" + 
"   			<td id=\"name-"
;
static private final String static_6 = "\">"
;
static private final String static_7 = "</td>\n" + 
"   			<td id=\"firstname-"
;
static private final String static_8 = "\">"
;
static private final String static_9 = "</td>\n" + 
"   			<td id=\"birthdate-"
;
static private final String static_10 = "\">"
;
static private final String static_11 = "</td>\n" + 
"   			<td id=\"email-"
;
static private final String static_12 = "\">"
;
static private final String static_13 = "</td>\n" + 
"   			<td><a href=\""
;
static private final String static_14 = "\">&gt;</a></td>\n" + 
"   		</tr>\n" + 
"	    "
;
static private final String static_15 = "	    <tr>\n" + 
"	        <form action=\""
;
static private final String static_16 = "\">\n" + 
"	        <td><input type=\"text\" name=\"contact.name\"></td>\n" + 
"	        <td><input type=\"text\" name=\"contact.firstname\"></td>\n" + 
"	        <td><input type=\"text\" name=\"contact.birthdate\"></td>\n" + 
"	        <td><input type=\"text\" name=\"contact.email\"></td>\n" + 
"	        <td><input type=\"submit\" value=\"+\"></td>\n" + 
"	        </form>\n" + 
"	    </tr>\n" + 
"	</tbody>\n" + 
"</table>\n" + 
"\n" + 
"<script type=\"text/javascript\" charset=\"utf-8\">\n" + 
"\n" + 
"    // In place edition\n" + 
"    $(\".contact td\").editInPlace({\n" + 
"        bg_over: 'transparent',\n" + 
"        callback: function(el, n, o) {\n" + 
"            var m = /([a-z]+)-(\\d+)/.exec(el), data = {}\n" + 
"            data['contact.id'] = m[2]\n" + 
"            data['contact.' + m[1]] = n\n" + 
"            \n" + 
"            // Save result\n" + 
"            $.ajax({\n" + 
"                url: '"
;
static private final String static_17 = "',\n" + 
"                type: 'POST',\n" + 
"                data: data,\n" + 
"                success: function() {$('#' + el).html(n)},\n" + 
"                error: function() {$('#' + el).html(o)}\n" + 
"            })\n" + 
"            \n" + 
"            return true\n" + 
"        }\n" + 
"    })\n" + 
"    \n" + 
"    // Drag & Drop\n" + 
"    var dragIcon = document.createElement('img')\n" + 
"    dragIcon.src = '"
;
static private final String static_18 = "'  \n" + 
"    //var action = #.   {jsAction @form(':id') /}    \n" + 
"    var action = "
;
static private final String static_19 = "    \n" + 
"    //var action = function(id) { return '/Application/form?id=' + id; }    \n" + 
"    var cancel = function cancel(e) {e.preventDefault()}\n" + 
"    \n" + 
"    $('#new')\n" + 
"        .bind('dragover', cancel)\n" + 
"        .bind('dragenter', cancel)\n" + 
"        .bind('drop', function(e) {\n" + 
"            document.location = action({id: e.originalEvent.dataTransfer.getData('contactId')})            \n" + 
"        })\n" + 
"      \n" + 
"    $('[draggable]').bind('dragstart', function(e) {\n" + 
"        e.originalEvent.dataTransfer.setData('contactId', $(this).attr('contactId'));\n" + 
"        e.originalEvent.dataTransfer.setDragImage(dragIcon, 0, -10);\n" + 
"    })\n" + 
"    \n" + 
"</script>"
;
	public list() {
		super(null);
	}
	public list(StringBuilder out) {
		super(out);
	}
	List<Contact> contacts;
	public cn.bran.japid.template.RenderResult render(List<Contact> contacts) {
		this.contacts = contacts;
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
_Each1.setActionRunners(getActionRunners());
_Each1.render(contacts, _Each1DoBody);
// line 17
p(static_15);// line 25
p(lookup("save", new Object[]{}));// line 27
p(static_16);// line 27
p(lookup("save", new Object[]{}));// line 50
p(static_17);// line 50
p(lookupStatic("public/images/avatar.png"));// line 63
p(static_18);// line 63
p(jsAction("form", ":id"));// line 65
p(static_19);// line 65

	}
	@Override protected void title() {
		p("List");;
	}
	private Each _Each1 = new Each(getOut());
class Each1DoBody implements Each.DoBody< Contact>{
	public void render(Contact contact, int _index, boolean _isOdd, String _parity, boolean _isFirst, boolean _isLast) {
		// line 17
p(static_4);// line 17
p(contact.id);// line 18
p(static_5);// line 18
p(contact.id);// line 19
p(static_6);// line 19
p(contact.name);// line 19
p(static_7);// line 19
p(contact.id);// line 20
p(static_8);// line 20
p(contact.firstname);// line 20
p(static_9);// line 20
p(contact.id);// line 21
p(static_10);// line 21
p(format(contact.birthdate, "yyyy-MM-dd"));// line 21
p(static_11);// line 21
p(contact.id);// line 22
p(static_12);// line 22
p(contact.email);// line 22
p(static_13);// line 22
p(lookup("form", contact.id));// line 23
p(static_14);// line 23

	}
}
	private Each1DoBody _Each1DoBody = new Each1DoBody();

}
