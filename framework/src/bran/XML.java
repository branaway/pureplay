package bran;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * use DOM3 standard to hide SUN's implementation.
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class XML {
	/**
	 * this thing is kind of expensive, taking > 4ms for a simple DOM. use Japid templates to render for best performance. 
	 * @param doc
	 * @return
	 */
	public static String Doc2Xml(Document doc) {
		try {
			LSSerializer serializer = getSerializer();
			long t = System.currentTimeMillis();
			String str = serializer.writeToString(doc);
			System.out.println("serialization of doc took/ms: " + (System.currentTimeMillis() - t));
			return str;
		} catch (Exception e) {
			return e.getMessage();
		}

	}

	static DOMImplementationRegistry registry;
	private static LSSerializer serializer;
	private static LSParser parser;

	private static synchronized LSSerializer getSerializer() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (serializer == null) {
			DOMImplementationRegistry reg = getRegistry();
			// DOMImplementation domImpl =
			// registry.getDOMImplementation("XML 3.0");
			// DOMImplementation dom3ls = registry.getDOMImplementation("LS");
			DOMImplementationLS dom3ls = (DOMImplementationLS) reg.getDOMImplementation("LS");
			serializer = dom3ls.createLSSerializer();
		}
		return serializer;
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private static synchronized DOMImplementationRegistry getRegistry() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		if (registry == null) {
			registry = DOMImplementationRegistry.newInstance();
		}
		return registry;
	}

	@Test
	public void testDoc2Xml() {
		try {
			LSParser parser = getParser();
			Document doc = parser.parseURI("Project.xml");
			long t = System.currentTimeMillis();
			String x = Doc2Xml(doc);
			System.out.println("serialization of doc took/ms: " + (System.currentTimeMillis() - t));
			assertTrue(x.contains("<name>PurePlay</name>"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private static synchronized LSParser getParser() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (parser == null) {
			DOMImplementationRegistry registry = getRegistry();
			DOMImplementationLS dom3ls = (DOMImplementationLS) registry.getDOMImplementation("LS");
			parser = dom3ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		}
		return parser;
	}
}
