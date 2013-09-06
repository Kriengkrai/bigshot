package bigshot.mp;

import java.util.concurrent.Callable;

/**
 * Callable that sets the name of the executing thread to the
 * name of the thread used to create the callable. Used to keep track
 * of work set callables.
 */
public abstract class ChildCallable<T> implements Callable<T> {
    
    /**
     * The name of the thread used to create the callable.
     * Set in constructor.
     */
    private final String newName;
    
    /**
     * Creates a new child callable and captures the name
     * of the creating thread.
     */
    public ChildCallable () {
        newName = Thread.currentThread ().getName ();
    }
    
    /**
     * Tucks away the current thread name, sets it to the
     * creating thread name, calls {@link #execute()},
     * then restores the old thread name.
     */
    public final T call () throws Exception {
        final String oldName = Thread.currentThread ().getName ();
        Thread.currentThread ().setName (newName);
        try {
            return execute ();
        } finally {
            Thread.currentThread ().setName (oldName);
        }
    }
    
    /**
     * Override in subclasses. This substitutes for 
     * {@code call()}.
     */
    protected abstract T execute () throws Exception;
}
