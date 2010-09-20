package bran;

import static name.pachler.nio.file.StandardWatchEventKind.ENTRY_CREATE;
import static name.pachler.nio.file.StandardWatchEventKind.ENTRY_DELETE;
import static name.pachler.nio.file.StandardWatchEventKind.ENTRY_MODIFY;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchEvent.Kind;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

public class FileWatch {
	WatchService watchService = FileSystems.getDefault().newWatchService();
	
	Map<WatchKey, FileWatcher> watched = new HashMap<WatchKey, FileWatcher>();
	
	public FileWatch() {
	}

	/**
	 * note: no recursive support. The watch list should be managed outside for complete directory tree monitoring
	 * @param watcher
	 */
	public void watch(FileWatcher watcher) {
		if (watched.containsValue(watcher))
			return;
		
		Path watchedPath = Paths.get(watcher.getPath());
		WatchKey key = null;
		try {
			// the recursive works in windows only! to bad
			key = watchedPath.register(watchService, new Kind[] {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
			watched.put(key, watcher);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				for(;;){
				    // take() will block until a file has been created/deleted
				    WatchKey key;
				    try {
				        key = watchService.take();
				    } catch (InterruptedException ix){
				        continue;
				    } catch (ClosedWatchServiceException cwse){
				        // other thread closed watch service
				        System.out.println("watch service closed, terminating.");
				        break;
				    }

				    // get list of events from key
				    FileWatcher fileWatcher = watched.get(key);
				    
				    
//					System.out.println("path key: " + path);
				    List<WatchEvent<?>> events = key.pollEvents();
				    // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
				    // key to be reported again by the watch service
				    key.reset();

				    // get the first to pass to monitor as a hint
				    WatchEvent<?> e = events.get(0);
			    	String fname = ((Path) e.context()).toString();
			    	Kind<?> kind = e.kind();
			    	// let the watcher to scan the directory for changes
			    	fileWatcher.notifyChange(fname, kind, FileWatch.this);
				    
//
//				    for(WatchEvent<?> e : events){
//				    	Path p = (Path) e.context();
//				    	Kind<?> kind = e.kind();
////				    	System.out.println(path + File.separator + p.toString() + ": " + kind.name());
//				    }
				}
			}
		};
		
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * test if p1 contains p2
	 * @param p1
	 * @param p2
	 */
	static boolean containPath(Path p1, Path p2) {
		return p1.toString().startsWith(p2.toString());
	}
}
