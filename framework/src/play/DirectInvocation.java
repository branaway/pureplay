package play;


/**
 * A direct invocation (in the same thread than caller)
 */
public abstract class DirectInvocation extends Invocation {

    Suspend retry = null;

    @Override
    public boolean init() {
        retry = null;
        return super.init();
    }

    @Override
    public void suspend(Suspend suspendRequest) {
        retry = suspendRequest;
    }

}