package bran;

import play.exceptions.PlayException;



public class CheckMessagesEvent extends PlayException {
	private static final long serialVersionUID = -8625897188710898903L;
	public String sessionId;

	public CheckMessagesEvent(String sessionId) {
		this.sessionId = sessionId;
	}


	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	@Override
	public String getErrorTitle() {
		return "check message";
	}


	@Override
	public String getErrorDescription() {
		return sessionId;
	}

}
