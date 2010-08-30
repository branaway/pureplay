package japidviews._javatags;

import cn.bran.play.JapidPlayAdapter;

/**
 * a well-know place to add all the static method you want to use in your
 * templates.
 * 
 * All the public static methods will be automatically "import static " to the
 * generated Java classes by the Japid compiler.
 * 
 */
public class JapidWebUtil {
	public static String hi() {
		return "Hi";
	}
	// your utility methods...
	
	/**
	 * XXX not working yet
	 */
	public static String jsAction(String action, String... args) {
		// action should look like:  /action?p1=:p1&p2=:p2
		action = JapidPlayAdapter.lookup(action, (Object[])args);
		StringBuilder sb = new StringBuilder();
		sb.append("function(options) {var pattern = '" + action.replace("&amp;", "&") + "';");
		sb.append("for(key in options) { pattern = pattern.replace(':'+key, options[key]); } return pattern };");
		return sb.toString();
	}
}
