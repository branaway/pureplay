package bran.objectpool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

/**
 * seems the two way of get a {@link ByteArrayOutputStream} does not show significant gain using the
 * checkin-checkout mode. might be slower!
 * 
 * XXX: we can consider putting the {@link ByteArrayOutputStream} to the {@link ThreadLocal} and reuse it!
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class BaosPoolTest {
	private static final String B = "我在测试一个字符输出流的速度我在测试一个字符输出流的速度我在测试一个字符输出流的速度";
	private static final byte[] B_ = B.getBytes();
	private static final String A = "我在测试一个字符输出流的速度";
	private static final String C = B + B + B + B + B + B + B + B + B + B;
	private static final String D = C + C + C + C + C + C + C + C + C + C;
	private static final byte[] C_ = C.getBytes();
	private static final byte[] D_ = D.getBytes();
	int len = 0;

	void addCount(int c) {
		len += c;
	}

	@Test
	public void testCreation() throws IOException, InterruptedException {
		final int count = 1;
		for (int i = 0; i < count; i++) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(A.getBytes());
			addCount(baos.size());
		}

		int threadCount = 2000;
		final CountDownLatch l = new CountDownLatch(1);
		final CountDownLatch ll = new CountDownLatch(threadCount);

		final BaosPool pool = new BaosPool();
		Runnable poolRunner = new Runnable() {
			@Override
			public void run() {
				try {
					l.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for (int i = 0; i < count; i++) {
					ByteArrayOutputStream baos = pool.checkout();
					try {
						baos.write(A.getBytes());
						// baos.write(B_);
						// baos.write(C_);
						baos.write(D_);
						addCount(baos.size());
						pool.checkin(baos);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				ll.countDown();
			}
		};

		List<Thread> ts = new ArrayList<Thread>();

		for (int i = 0; i < threadCount; i++) {
			Thread usePool = new Thread(poolRunner);
			ts.add(usePool);
		}

		long t = 0;

		for (Thread th : ts) {
			th.start();
		}
		t = System.currentTimeMillis();
		l.countDown();

		ll.await();

		// lets get it from the pool
		long x = System.currentTimeMillis() - t;
		System.out.println("pool took/ms: " + x);

		final CountDownLatch l2 = new CountDownLatch(1);
		final CountDownLatch l2l2 = new CountDownLatch(threadCount);

		Runnable simpleRunner = new Runnable() {
			@Override
			public void run() {
				try {
					l2.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for (int i = 0; i < count; i++) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						baos.write(A.getBytes());
						baos.write(D_);
						addCount(baos.size());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				l2l2.countDown();
			}
		};

		ts = new ArrayList<Thread>();

		for (int i = 0; i < threadCount; i++) {
			Thread usePool = new Thread(simpleRunner);
			ts.add(usePool);
		}

		for (Thread th : ts) {
			th.start();
		}
		t = System.currentTimeMillis();
		l2.countDown();

		l2l2.await();

		x = System.currentTimeMillis() - t;
		System.out.println("simple new took/ms: " + x);

	}
}
