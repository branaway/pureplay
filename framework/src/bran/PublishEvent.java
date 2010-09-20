package bran;

import play.exceptions.PlayException;

public class PublishEvent extends PlayException {
	private static final long serialVersionUID = 2487624035276689952L;
	String topic;
	Message msg;

	public PublishEvent(String topic, Message msg) {
		super();
		this.topic = topic;
		this.msg = msg;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	@Override
	public String getErrorTitle() {
		return topic;
	}

	@Override
	public String getErrorDescription() {
		return msg.contentString;
	}
	
}
