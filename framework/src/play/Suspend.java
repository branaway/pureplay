package play;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import play.exceptions.PlayException;

/**
 * Throwable to indicate that the request must be suspended
 */
public class Suspend extends PlayException {

    /**
     * Suspend for a timeout (in milliseconds).
     */
    long timeout;
    
    /**
     * Wait for task execution.
     */
    List<Future<?>> tasks;

    public Suspend(long timeout) {
        this.timeout = timeout;
    }

    public Suspend(Future<?>... tasks) {
        this.tasks = Arrays.asList(tasks);
    }

    @Override
    public String getErrorTitle() {
        return "Request is suspended";
    }

    @Override
    public String getErrorDescription() {
        if (tasks != null) {
            return "Wait for " + tasks;
        }
        return "Retry in " + timeout + " ms.";
    }
}