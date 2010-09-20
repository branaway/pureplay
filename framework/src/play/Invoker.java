package play;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import play.Play.Mode;
import play.exceptions.UnexpectedException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Run some code in a Play! context
 */
public class Invoker {
	/**
	 * Main executor for requests invocations.
	 */
	public static ScheduledThreadPoolExecutor executor = null;

	static {
        int core = Integer.parseInt(Play.configuration.getProperty("play.pool", Play.mode == Mode.DEV ? "1" : ((Runtime.getRuntime().availableProcessors()+1) + "")));
        System.out.println("Number of threads to run actions: " + core);
        executor = new ScheduledThreadPoolExecutor(core, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Run the code in a new thread took from a thread pool.
     * @param invocation The code to run
     * @return The future object, to know when the task is completed
     */
    public static Future<?> invoke(final Invocation invocation) {
        Monitor monitor = MonitorFactory.getMonitor("Invoker queue size", "elmts.");
        monitor.add(executor.getQueue().size());
        invocation.waitInQueue = MonitorFactory.start("Waiting for execution");
        return executor.submit(invocation);
    }

    /**
     * Run the code in a new thread after a delay
     * @param invocation The code to run
     * @param millis The time to wait before, in milliseconds
     * @return The future object, to know when the task is completed
     */
    public static Future<?> invoke(final Invocation invocation, long millis) {
        Monitor monitor = MonitorFactory.getMonitor("Invocation queue", "elmts.");
        monitor.add(executor.getQueue().size());
        return executor.schedule(invocation, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Run the code in the same thread than caller.
     * @param invocation The code to run
     */
    public static void invokeInThread(DirectInvocation invocation) {
        boolean retry = true;
        while (retry) {
            invocation.run();
            if (invocation.retry == null) {
                retry = false;
            } else {
                try {
                    if (invocation.retry.tasks != null) {
                        for(Future<?> f : invocation.retry.tasks) f.get();
                    } else {
                        Thread.sleep(invocation.retry.timeout);
                    }
                } catch (Exception e) {
                    throw new UnexpectedException(e);
                }
                retry = true;
            }
        }
    }


}
