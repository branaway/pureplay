package play.mvc;

import jregex.Pattern;

import org.junit.Test;


public class RouteTest {
	// the rule pattern 
	// GET|POST|*|...(headers)				/my/path		controller.action(id:'home'...)
	public static final String routeRule = 
		"^({method}GET|POST|PUT|DELETE|OPTIONS|HEAD|\\*)[(]?({headers}[^)]*)(\\))?\\s+({path}.*/[^\\s]*)\\s+({action}[^\\s(]+)({params}.+)?(\\s*)$";
	
	@Test
	public void testF1() {
		Route r = new Route() {{
			method = "GET";
			path = "/";
			action = "Application.index";
		}};
		
		r.compute();
		System.out.println(r);
	}
	@Test
	public void testF2() {
		Route r = new Route() {{
			method = "GET";
			path = "/hello/{<[a-z]{4,10}>who}";
			action = "Application.hello2(count:20)";
		}};
		
		r.compute();
		System.out.println(r);
	}
}
