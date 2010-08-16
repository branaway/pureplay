package play.mvc;

import java.io.InputStream;
import play.mvc.Controller;
import java.util.concurrent.Future;
import java.io.File;

/**
 * creates a delegate which can be used to take over play.mvc.Controller namespace with a type
 * alias. Extending from this class means that we can avoid circular references which would
 * occur if ScalaController was inhereted directly from @see play.mvc.Controller and we used a type alias
 * to map ScalaController to play.mvc.Controller
 */
abstract class ControllerDelegate {
    
    public static void render(Object... args) {
        Controller.render(args);
    }
    
    public void renderTemplate(String template, Object... args) {
        Controller.renderTemplate(template,args);
    }

    public void renderText(Object text) {
        Controller.renderText(text);
    }

    public void renderText(CharSequence pattern, Object... args) {
        Controller.renderText(pattern, args);
    }

    public void renderXml(String xml) {
        Controller.renderXml(xml);
    }

    public void renderJSON(String json) {
        Controller.renderJSON(json);
    }

    public void renderJSON(Object anyObject) {
        Controller.renderJSON(anyObject);
    }

    public void notModified() {
        Controller.notModified();
    }

    public void todo() {
        Controller.todo();
    }

    public void redirectToStatic(String file) {
        Controller.redirectToStatic(file);
    }

    public void redirect(String url) {
        Controller.redirect(url);
    }


    public void redirect(String url, boolean permanent) {
        Controller.redirect(url, permanent);
    }

    public void redirect(String action, Object... args) {
       Controller.redirect(action, args);
    }

    public void redirect(String action, boolean permanent, Object... args) {
        Controller.redirect(action, permanent, args);
    }

    public void unauthorized(String realm) {
        Controller.unauthorized(realm);
    }

    public void unauthorized() {
        Controller.unauthorized("");
    }

    public void notFound(String what) {
        Controller.notFound(what);
    }

    public void notFound() {
        Controller.notFound("");
    }

    public void notFoundIfNull(Object o) {
        Controller.notFoundIfNull(o);
    }

    public void ok() {
        Controller.ok();
    }
    public void error(String reason) {
       Controller.error(reason);	 
    }

    public void error(Exception reason) {
       Controller.error(reason);	 
    }

    public void error() {
       Controller.error();	 
    }

    public void error(int status, String reason) {
       Controller.error(status,reason);	 
    }

    public void renderBinary(InputStream stream) {
        Controller.renderBinary(stream);
    }

    public void renderBinary(File file) {
        Controller.renderBinary(file);
    }
    
    public void renderBinary(InputStream stream, String name) {
        Controller.renderBinary(stream, name);
    }

    public void forbidden() {
        Controller.forbidden();
    }

    public void waitFor(Future task) {
        Controller.waitFor(task);
    }

}
