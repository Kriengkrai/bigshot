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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of the {@link WorkSetFactory} interface.
 */
public class DefaultWorkSetFactory implements WorkSetFactory, Runnable {
    
    private volatile boolean disposed = false;
    private final Thread[] threads;
    private final LinkedList<FutureTaskImpl<?>> tasks = new LinkedList<FutureTaskImpl<?>> ();
    
    public DefaultWorkSetFactory () {
        threads = new Thread[Runtime.getRuntime ().availableProcessors ()];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread (this);
            threads[i].setDaemon (true);
            threads[i].start ();
        }
    }
    
    public <R> WorkSet<R> create () {
        return new WorkSetImpl<R> (this);
    }
    
    public void run () {
        while (!disposed) {
            FutureTaskImpl<?> task = null;
            synchronized (tasks) {
                while (!disposed) {
                    if (tasks.size () > 0) {
                        task = tasks.removeFirst ();
                        break;
                    } else {
                        try {
                            tasks.wait ();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace ();
                            return;
                        }
                    }
                }
            }
            if (task != null) {
                task.run ();
            }
        }
    }
    
    public <T> Future<T> submit (Callable<T> callable) {
        FutureTaskImpl<T> result = new FutureTaskImpl<T> (callable);
        synchronized (tasks) {
            tasks.add (result);
            tasks.notify ();
        }
        return result;
    }
    
    private static class FutureTaskImpl<T> extends FutureTask<T> implements Future<T> {
        
        public FutureTaskImpl (Callable<T> callable) {
            super (callable);
        }
        
        public T get () throws  InterruptedException, ExecutionException {
            run ();
            return super.get ();
        }
        
        public T get (long timeout, TimeUnit unit) throws  InterruptedException, ExecutionException, TimeoutException {
            run ();
            return super.get (timeout, unit);
        }
        
        public synchronized void run () {
            if (!isDone () && !isCancelled ()) {
                super.run ();
            }
        }
    }
    
    public void dispose () {
        disposed = true;
        synchronized (tasks) {
            tasks.notifyAll ();
        }
    }
    
    private static class WorkSetImpl<R> extends WorkSet<R> {
        
        private final List<Future<R>> futures = new ArrayList<Future<R>> ();
        private final DefaultWorkSetFactory executor;
        
        public WorkSetImpl (DefaultWorkSetFactory executor) {
            this.executor = executor;
        }
        
        public List<Future<R>> complete () throws Exception {
            // Iterate over the futures. Those that are 
            // already completed return immediately,
            // Those that are still waiting will execute
            // in this thread.
            for (Future<R> f : futures) {
                f.get ();
            }
            return futures;
        }
        
        public List<R> join () throws Exception {
            // Complete and gather up the results.
            
            List<R> result = new ArrayList<R> ();
            for (Future<R> f : complete ()) {
                result.add (f.get ());
            }
            return result;
        }
        
        public void execute (Callable<R> callable) {
            futures.add (executor.submit (callable));
        }
        
        public void parallelFor (int a, int b, final ParallelFor<R> pfor) throws Exception {
            int delta = b - a;
            int step = delta / (Runtime.getRuntime ().availableProcessors () * 2);
            if (step < 1) {
                step = 1;
            }
            int i = a;
            while (i < b) {
                int start = i;
                int end = i + step;
                if (end > b) {
                    end = b;
                }
                final int fs = start;
                final int fe = end;
                execute (new ChildCallable<R> () {
                        public R execute () throws Exception {
                            return pfor.call (fs, fe);
                        }
                    });
                
                i = end;
            }
        }
    }
}