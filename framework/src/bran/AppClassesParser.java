package bran;

import java.util.List;

import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.mvc.Controller;

public class AppClassesParser {
	public String parse(ApplicationClasses clss) {
		List<ApplicationClass> classes = clss.getAssignableClasses(Controller.class);
		
		
		return null;
	}
}
