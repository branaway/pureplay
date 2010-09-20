package bran;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;


public class MessageTest {
	@Test
	public void t1() {
		Message m = new Message();
		m.from = "冉兵";
		m.sendTime  = new Date();
		m.topic = "greetings";
		m.contentString = "hello there!";
		String s = m.toString();
		System.out.println(s);
		assertTrue(s.contains("From: 冉兵"));
	}

}
