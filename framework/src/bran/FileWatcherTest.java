package bran;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import name.pachler.nio.file.WatchEvent.Kind;

import org.junit.Test;

public class FileWatcherTest {
	private static final String root = "test/bran";

	static class MyFileWatcher extends FileWatcher {
		public MyFileWatcher(String path) {
			super(path);
		}

		@Override
		public void notifyChange(String fname, Kind<?> kind, FileWatch fw) {
			String file = path + File.separator + fname;
			System.out.println("got: " + file + ":" + kind.name());
			File f = new File(file);
			if (f.isDirectory()) {
				if (kind == name.pachler.nio.file.StandardWatchEventKind.ENTRY_CREATE) {
					// add new monitor
					System.out.println("add new monitor for " + file);
					fw.watch(new MyFileWatcher(file));
				}
				else if (kind == name.pachler.nio.file.StandardWatchEventKind.ENTRY_MODIFY) {
					// funny removing a dir seems to give a MODIFY?
				}
			} else {
				// System.out.println("add new monitor for " + file);
				// TODO: add a delay to trigger heavy weight stuff in case
				// multiple events poured in
			}
		}
	}

	@Test
	public void testWatcher() throws IOException {
		MyFileWatcher fw = new MyFileWatcher(root);
		FileWatch fileWatch = new FileWatch();
		fileWatch.watch(fw);
		fileWatch.start();
		File dir = new File(root, "dir0");
		dir.mkdir();
		File f = new File(dir, "testfile.txt");
		FileOutputStream os = new FileOutputStream(f);
		os.write("hello".getBytes());
		os.close();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(f.delete());
		assertTrue(dir.delete());
	}
}
