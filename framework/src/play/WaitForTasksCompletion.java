package play;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
     * Utility that track tasks completion in order to resume suspended requests.
     * 
     * XXX bran: There might be threading issue with this class!
     * 
     */
    class WaitForTasksCompletion extends Thread {

        Map<List<Future<?>>, Invocation> queue;
        static WaitForTasksCompletion instance;

        public WaitForTasksCompletion() {
            queue = new ConcurrentHashMap<List<Future<?>>, Invocation>();
            setName("WaitForTasksCompletion");
            setDaemon(true);
            start();
        }

//        public static void waitFor(Future<?> task, Invocation invocation) {
//            init();
//            instance.queue.put(task, invocation);
//        }
//
		private synchronized static void init() {
            if (instance == null) {
                instance = new WaitForTasksCompletion();
            }
		}
		
		public static void waitFor(List<Future<?>> tasks, Invocation invocation) {
        	init();
        	instance.queue.put(tasks, invocation);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (!queue.isEmpty()) {
                        for (List<Future<?>> tasks : new HashSet<List<Future<?>>>(queue.keySet())) {
                            boolean allDone = true;
                            for(Future<?> f : tasks) {
                                if(!f.isDone()) {
                                    allDone = false;
                                }
                            }
                            if (allDone) {
                                Invoker.executor.submit(queue.get(tasks));
                                queue.remove(tasks);
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.warn(ex, "");
                }
            }
        }
    }