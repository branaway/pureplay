package bran;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import play.server.NettyInvocationDirect;

/**
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class SessionInfo implements Comparable<SessionInfo>{
	String sessionId;
	private static final int SESSION_TTL = 50000;
	long lastCheckinTime;
	Set<String> topics = new HashSet<String>();
	// set this to null once the current invocation checks out.
	NettyInvocationDirect invocation;
	BlockingQueue<Message> inbox = new LinkedBlockingQueue<Message>();
	
	public SessionInfo(String sessionId) {
		this.sessionId = sessionId;
	}

	public synchronized NettyInvocationDirect checkout() {
		NettyInvocationDirect result = invocation;
		invocation = null;
		return result;
	}

	public void queueMsg(Message msg) {
		inbox.add(msg);
	}

	public boolean expired(long now) {
		if (this.invocation == null && now - lastCheckinTime > SESSION_TTL) {
			return true;
		}
		return false;
	}

	public void addInterest(String topic) {
		topics.add(topic);
	}

	public void checkin(NettyInvocationDirect invoke) {
		this.lastCheckinTime = System.currentTimeMillis();
		this.invocation = invoke;
	}

	@Override
	public int compareTo(SessionInfo o) {
		return this.sessionId.compareTo(o.sessionId);
	}
}