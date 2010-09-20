package play;

import play.Play.Mode;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.exceptions.PlayException;
import play.exceptions.UnexpectedException;

import com.jamonapi.Monitor;

/**
 * An Invocation in something to run in a Play! context
 */
public abstract class Invocation implements Runnable {
    
    /**
     * If set, monitor the time the invocation waited in the queue
     */
    Monitor waitInQueue;

    /**
     * Override this method
     * @throws java.lang.Exception
     */
    public abstract void execute() throws Exception;

    /**
     * Init the call (especially useful in DEV mode to detect changes)
     */
    public boolean init() {
        Thread.currentThread().setContextClassLoader(Play.classloader);
        Play.detectChanges();
        if (!Play.started) {
            if (Play.mode == Mode.PROD) {
                throw new UnexpectedException("Application is not started");
            }
            Play.start();
        }
        return true;
    }

    /**
     * Things to do before an Invocation
     */
    public void before() {
        Thread.currentThread().setContextClassLoader(Play.classloader);
        for (PlayPlugin plugin : Play.plugins) {
            plugin.beforeInvocation();
        }
    }

    /**
     * Things to do after an Invocation.
     * (if the Invocation code has not thrown any exception)
     */
    public void after() {
        for (PlayPlugin plugin : Play.plugins) {
            plugin.afterInvocation();
        }
        LocalVariablesNamesTracer.checkEmpty(); // detect bugs ....
    }

    /**
     * Things to do if the Invocation code thrown an exception
     */
    public void onException(Throwable e) {
        for (PlayPlugin plugin : Play.plugins) {
            try {
                plugin.onInvocationException(e);
            } catch (Throwable ex) {
            }
        }
        if (e instanceof PlayException) {
            throw (PlayException) e;
        }
        throw new UnexpectedException(e);
    }

    /**
     * The request is suspended
     * @param suspendRequest
     */
    public void suspend(Suspend suspendRequest) {
        if (suspendRequest.tasks != null) {
            WaitForTasksCompletion.waitFor(suspendRequest.tasks, this);
        } else {
            Invoker.invoke(this, suspendRequest.timeout);
        }
    }

    /**
     * Things to do in all cases after the invocation.
     */
    public void _finally() {
        for (PlayPlugin plugin : Play.plugins) {
            plugin.invocationFinally();
        }
    }

    /**
     * It's time to execute.
     */
    @Override
	public void run() {
        if(waitInQueue != null) {
            waitInQueue.stop();
        }
        try {
            if (init()) {
                before();
                execute();
                after();
            }
        } catch (Suspend e) {
            suspend(e);
            after();
        } catch (Throwable e) {
            onException(e);
        } finally {
            _finally();
        }
    }

}