package bran;

import name.pachler.nio.file.WatchEvent.Kind;


public abstract class FileWatcher implements Comparable<FileWatcher> {
	String path;
	
	public FileWatcher(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	abstract public void notifyChange(String fname, Kind<?> kind, FileWatch fw);

	@Override
	public int compareTo(FileWatcher o) {
		return path.compareTo(o.path);
	}
	
}
