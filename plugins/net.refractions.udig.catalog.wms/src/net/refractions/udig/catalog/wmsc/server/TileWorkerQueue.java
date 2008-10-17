package net.refractions.udig.catalog.wmsc.server;

import java.util.LinkedList;

/**
 * This is a work queue for re-using a group of threads to do Tile work.  An example use
 * is the work of saving tiles to disk.  When preloading all tiles in a tileset, the number of
 * threads originally got out of hand for saving tiles.  This queue allows a group
 * of threads to be reused to do the work.  It can also be used to manage the threads
 * for sending tile requests out, but there should be a separate queue for each.
 * 
 * The base of this class was copied from IBM's online resource
 * "Java theory and practice: Thread pools and work queues"
 * @see http://www.ibm.com/developerworks/library/j-jtp0730.html
 * 
 * @author GDavis
 *
 */
public class TileWorkerQueue {
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    public static final int defaultWorkingQueueSize = 16;

    public TileWorkerQueue(int nThreads)
    {
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i=0; i<this.nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try
                        {
                            queue.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                    }

                    r = (Runnable) queue.removeFirst();
                }

                // If we don't catch RuntimeException, 
                // the pool could leak threads
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }
    
    /*
     * Stop and delete all the threads
     */
    public void dispose() {
        for (int i=0; i<this.nThreads; i++) {
            threads[i] = null;
        }
    }
}

