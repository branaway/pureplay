package bran;

import play.Invocation;
import play.exceptions.PlayException;


public class SubscribeEvent extends PlayException {
	private static final long serialVersionUID = 7993824090304215663L;
	String topic;
	String sessionId;
	Invocation invocation;

	public SubscribeEvent(String topic, String sessionId) {
		this.topic = topic;
		this.sessionId = sessionId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Invocation getInvocation() {
		return invocation;
	}

	public void setInvocation(Invocation invocation) {
		this.invocation = invocation;
	}

	@Override
	public String getErrorTitle() {
		return topic;
	}

	@Override
	public String getErrorDescription() {
		return sessionId;
	}
	
}
