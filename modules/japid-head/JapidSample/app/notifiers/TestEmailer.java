package notifiers;

import models.japidsample.Post;

import org.apache.commons.mail.EmailAttachment;

import play.Play;
import cn.bran.play.JapidMailer;

public class TestEmailer extends JapidMailer {
	public static void emailme(Post post) {
		setSubject("Welcome %s", "me");
		addRecipient("me@me.com");
		setFrom("Me <me@me.com>");
		// EmailAttachment attachment = new EmailAttachment();
		// attachment.setDescription("A pdf document");
		// attachment.setPath(Play.getFile("rules.pdf").getPath());
		// addAttachment(attachment);
		send(post);
	}
}
