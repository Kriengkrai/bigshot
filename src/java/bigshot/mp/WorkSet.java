/*
 * Copyright 2010 - 2013 Leo Sutic <leo.sutic@gmail.com>
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package bigshot.mp;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.List;
import java.util.Collection;

/**
 * A work set abstraction.
 */
public abstract class WorkSet<Result> {
    
    /**
     * The pluggable factory implementation to use.
     */
    private static WorkSetFactory factory;
    static {
        factory = new DefaultWorkSetFactory ();
    }
    
    /**
     * Creates a new work set.
     */
    public static <Result> WorkSet<Result> create () {
        return factory.create ();
    }
    
    /**
     * Set the factory implementation to use.
     */
    public static void setFactory (WorkSetFactory factory) {
        if (WorkSet.factory != null) {
            WorkSet.factory.dispose ();
        }
        WorkSet.factory = factory;
    }
    
    /**
     * Completes all submitted callables and returns the results as {@link Future}s.
     */
    public abstract List<Future<Result>> complete () throws Exception;
    
    /**
     * Completes all submitted callables, also in the calling thread if possible,
     * and returns the results.
     */
    public abstract List<Result> join () throws Exception;
    
    /**
     * Submits a callable for execution.
     */
    public abstract void execute (Callable<Result> callable);
    
    /**
     * Executes a parallel for in this work set.
     */
    public abstract void parallelFor (int a, int b, ParallelFor<Result> pfor) throws Exception;
    
    /**
     * Convenience method to execute a parallel for in a work set.
     */
    public static <Result> List<Result> pfor (int a, int b, ParallelFor<Result> pfor) throws Exception {
        WorkSet<Result> res = WorkSet.create ();
        res.parallelFor (a, b, pfor);
        return res.join ();
    }
}