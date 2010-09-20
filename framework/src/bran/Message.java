package bran;

import java.util.Date;

public class Message {
	public String contentString;
	public String from;
	public Date sendTime;
	public String topic;
	
	@Override
	public String toString() {
		String form = "From: %s; Topic: %s; Time: %s; Content: %s";
		return String.format(form, from, topic, sendTime, contentString);
	}
	// subject?
	// shoudl there be receivers?
}
