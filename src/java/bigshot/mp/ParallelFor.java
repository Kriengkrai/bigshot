package bigshot.mp;

/**
 * Interface for parallel for-loops.
 */
public interface ParallelFor<Result> {
    /**
     * Execute a for loop between a and b, like <code>for (int i = a; i &lt; b; ++i) { ... }</code>
     * and return the result.
     */
    public Result call (int a, int b) throws Exception;
}