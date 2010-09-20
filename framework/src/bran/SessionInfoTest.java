package bran;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Test;


public class SessionInfoTest {
	@Test
	public void usedInset() throws InterruptedException {
		final ConcurrentSkipListSet<SessionInfo> set = new ConcurrentSkipListSet<SessionInfo>();
		Thread ta = new Thread(new Runnable() {
			@Override
			public void run() {
				SessionInfo a = new SessionInfo("a");
				set.add(a);
			}
		});
		
		Thread tb = new Thread(new Runnable() {
			@Override
			public void run() {
				SessionInfo a = new SessionInfo("b");
				set.add(a);
			}
		});
		
		ta.start();
		tb.start();
	
		ta.join();
		tb.join();
		
		assertEquals(2, set.size());

		SessionInfo b = new SessionInfo("b");
		set.remove(b);
		assertEquals(1, set.size());
	}
}
