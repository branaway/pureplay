package bran.objectpool;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * a pool to store {@link ByteArrayOutputStream} instances, which potentially has been expanded to contain 
 * relatively big content, to avoid generating memory garbage.
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class BaosPool {
	LinkedBlockingQueue<ByteArrayOutputStream> pool = new LinkedBlockingQueue<ByteArrayOutputStream>();
	
	public ByteArrayOutputStream checkout() {
		ByteArrayOutputStream poll = pool.poll();
		if (poll == null) {
			return new ByteArrayOutputStream();
		}
		else {
			return poll;
		}
	}
	
	public void checkin(ByteArrayOutputStream baos) {
		baos.reset();
		pool.add(baos);
	}
}
