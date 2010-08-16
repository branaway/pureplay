package cn.bran.play;

import cn.bran.japid.template.ActionRunner;
import cn.bran.japid.template.RenderResult;

/**
 * The class defines a cached adapter to invoke a piece of code.
 * 
 * This class can be used in actions to run a piece of rendering code
 * conditionally
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public abstract class CacheableRunner extends ActionRunner {
	private Object[] key = null;
	private String ttlAbs = null;
	private String keyString;
	private boolean noCache;
	private boolean readThru;

	/**
	 * 
	 * @param ttl
	 *            time to live for the cached item, format in "1s", "2mn", "3h",
	 *            "4d" for example, "29d" max
	 * @param args
	 *            objects to make a key from their string values
	 */
	public CacheableRunner(String ttl, Object... args) {
		super();
		this.key = args;
		if (key == null || key.length == 0)
			this.noCache = true;

		if (ttl == null || ttl.trim().length() == 0) {
			this.noCache = true;
		} else {
			if (ttl.startsWith("-")) {
				this.ttlAbs = ttl.substring(1);
				this.readThru = true;
			} else {
				this.ttlAbs = ttl;
			}
		}

		this.keyString = buildKey(key);

	}

	/**
	 * I'm throwing a JapidResult rather than return a RenderResult. so users in
	 * action don't need to. Convenience.
	 */
	@Override
	public RenderResult run() {
		if (noCache) {
			return render();
		} else {
			RenderResult rr = null;
			if (!readThru) {
				try {
					rr = RenderResultCache.get(keyString);
					if (rr != null)
						return rr;
				} catch (ShouldRefreshException e) {
				}
			}

			RenderResult rr1;
			try {
				rr1 = render();
				RenderResultCache.set(keyString, rr1, ttlAbs);
				return (rr1);
			} catch (JapidResult e) {
				rr1 = e.getRenderResult();
				RenderResultCache.set(keyString, rr1, ttlAbs);
				return rr1;
			}
		}
		//
	}

	/**
	 * @return
	 */
	private static String buildKey(Object[] keys) {
		String keyString = "";
		for (Object k : keys) {
			keyString += ":" + String.valueOf(k);
		}
		if (keyString.startsWith(":"))
			if ( keyString.length() > 1)
				keyString = keyString.substring(1);
			else
				keyString = "";
		return keyString;
	}

	/**
	 * the render method can either return a RenderResult or throw a JapidResult
	 * 
	 * @return
	 */
	protected abstract RenderResult render();

	public static void deleteCache(Object...objects) {
		RenderResultCache.delete(buildKey(objects));
	}
}