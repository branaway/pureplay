package play.server;

import java.io.ByteArrayOutputStream;

public class BaosThreadStore {
	static ThreadLocal<ByteArrayOutputStream> tl = new ThreadLocal<ByteArrayOutputStream>() {

		@Override
		protected ByteArrayOutputStream initialValue() {
			return new ByteArrayOutputStream();
		}
	};
	
	public static ByteArrayOutputStream checkout() {
		ByteArrayOutputStream baos = tl.get();
		baos.reset();
		return baos;
	}
	
}
