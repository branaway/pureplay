package play.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import play.Logger;
import play.Play;
import play.exceptions.ConfigurationException;

/**
 * Memcached implementation (using http://code.google.com/p/spymemcached/)
 */
public class MemcachedImpl implements CacheImpl {

    private static MemcachedImpl uniqueInstance;

    public static MemcachedImpl getInstance() throws IOException {
        if (uniqueInstance == null) {
            uniqueInstance = new MemcachedImpl();
        }
        return uniqueInstance;
    }
    MemcachedClient client;
    SerializingTranscoder tc;

    private MemcachedImpl() throws IOException {
        tc = new SerializingTranscoder() {

            @Override
            protected Object deserialize(byte[] data) {
                try {
                    return new ObjectInputStream(new ByteArrayInputStream(data)) {

                        @Override
                        protected Class<?> resolveClass(ObjectStreamClass desc)
                                throws IOException, ClassNotFoundException {
							try {
								Class<?> loadClass = Play.classloader.loadClass(desc.getName());
//								System.out.println("class resolved to: " + loadClass.getName());
								return loadClass;
							} catch (Exception e) {
								// bran: must do this, or some class such as [C won't
								// get resolved. Might be an inadequate impl in
								// the Play.classloader
								return super.resolveClass(desc);
							}
						}
                    }.readObject();
                } catch (Exception e) {
                    Logger.error(e, "Could not deserialize");
                }
                return null;
            }

            @Override
            protected byte[] serialize(Object object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    new ObjectOutputStream(bos).writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    Logger.error(e, "Could not serialize");
                }
                return null;
            }
        };

        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        if (Play.configuration.containsKey("memcached.host")) {
        	// bran changed to use the binary protocol
            client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(Play.configuration.getProperty("memcached.host")));
        } else if (Play.configuration.containsKey("memcached.1.host")) {
            int nb = 1;
            String addresses = "";
            while (Play.configuration.containsKey("memcached." + nb + ".host")) {
                addresses += Play.configuration.get("memcached." + nb + ".host") + " ";
                nb++;
            }
            client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(addresses));
//            client = new MemcachedClient(AddrUtil.getAddresses(addresses));
        } else {
            throw new ConfigurationException(("Bad configuration for memcached"));
        }
    }

    @Override
	public void add(String key, Object value, int expiration) {
    	// bran: encode
 		key = clean(key);
        client.add(key, expiration, value, tc);
    }

	/**
	 * @param key
	 * @return
	 * @author bran
	 */
	private String clean(String key) {
		key = key.replace(' ', '_');
		return key;
	}

    @Override
	public Object get(String key) {
        Future<Object> future = client.asyncGet(clean(key), tc);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return null;
    }

    @Override
	public void clear() {
        client.flush();
    }

    @Override
	public void delete(String key) {
        client.delete(clean(key));
    }

    @Override
	public Map<String, Object> get(String[] keys) {
    	String[] ks = cleanKeys(keys);
        Future<Map<String, Object>> future = client.asyncGetBulk(tc, ks);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return new HashMap<String, Object>();
    }

	/**
	 * @param keys
	 * @return
	 * @author bran
	 */
	private String[] cleanKeys(String[] keys) {
		String[] ks = new String[keys.length];
    	for (int i = 0; i < keys.length; i++) {
    		ks[i] = clean(keys[i]);
    	}
		return ks;
	}

    @Override
	public long incr(String key, int by) {
        return client.incr(clean(key), by);
    }

    @Override
	public long decr(String key, int by) {
        return client.decr(clean(key), by);
    }

   @Override
 public void replace(String key, Object value, int expiration) {
        client.replace(clean(key), expiration, value, tc);
    }

    @Override
	public boolean safeAdd(String key, Object value, int expiration) {
        Future<Boolean> future = client.add(clean(key), expiration, value, tc);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

    @Override
	public boolean safeDelete(String key) {
        Future<Boolean> future = client.delete(clean(key));
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

    @Override
	public boolean safeReplace(String key, Object value, int expiration) {
        Future<Boolean> future = client.replace(clean(key), expiration, value, tc);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

   @Override
 public boolean safeSet(String key, Object value, int expiration) {
        Future<Boolean> future = client.set(clean(key), expiration, value, tc);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(false);
        }
        return false;
    }

    @Override
	public void set(String key, Object value, int expiration) {
        client.set(clean(key), expiration, value, tc);
    }

    @Override
	public void stop() {
        client.shutdown();
    }
}
