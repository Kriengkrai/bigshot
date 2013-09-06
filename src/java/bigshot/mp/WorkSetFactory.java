package bigshot.mp;

/**
 * Pluggable factory for {@link WorkSet}s.
 */
public interface WorkSetFactory {
    /**
     * Create a new work set with the given result type.
     */
    public <Result> WorkSet<Result> create ();
    
    /**
     * Dispose this factory.
     */
    public void dispose ();
}