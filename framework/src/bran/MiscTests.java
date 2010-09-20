package bran;

import static org.junit.Assert.*;

import org.junit.Test;

import play.libs.Codec;

public class MiscTests {
	@Test
	public void testUUIDLength() {
		String uuid = Codec.UUID();
//		System.out.println(uuid);
		assertEquals(36, uuid.length());
	}
}
