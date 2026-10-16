package icbm.classic.lib.thread;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigDebug;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public class WorkerThread extends Thread
{
    /** Written by the server thread on shutdown, volatile so the worker sees it */
    public volatile boolean doRun = true;

    private IThreadWork activateTask;
    private final ConcurrentLinkedQueue<IThreadWork> queue = new ConcurrentLinkedQueue();
    /** Updated by the server thread (add) and the worker (poll) */
    private final AtomicInteger workCount = new AtomicInteger();

    public WorkerThread(int index)
    {
        super(null, null, "ICBM-Classic-WorkerThread-" + index, 0);
        this.setPriority(Thread.MIN_PRIORITY); //We don't care how fast this runs
        this.setDaemon(true); //Fix for threads still running when MC closes
    }

    @Override
    public void interrupt()
    {
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().error(toString() + " was interrupted while running tasks",
                    new RuntimeException("Trace"));
        }
        super.interrupt();
    }

    @Override
    public final void run()
    {
        while (doRun)
        {
            //IF task is set run
            if (activateTask != null)
            {
                // PORT: 1.12 had no error handling here, a single failing task (modded block, chunk access) ended
                //  run() and every later threaded blast was queued to a dead thread and never finished.
                try
                {
                    //Run step
                    if (!activateTask.doRun(1))
                    {
                        // PORT: threads are stopped on ServerStoppingEvent, results of a task that finishes after that
                        //  would be applied to levels that are being saved and closed, so the task is dropped
                        if (doRun)
                        {
                            //Complete
                            finishTask();
                        }
                        else
                        {
                            activateTask = null;
                        }
                    }
                }
                catch (Throwable t)
                {
                    ICBMClassic.logger().error(toString() + " work task failed " + activateTask, t);

                    // Still complete so the work rejoins the world with what it has and the owner can end,
                    //  unless the threads were stopped for server shutdown (see above)
                    if (doRun)
                    {
                        finishTask();
                    }
                    else
                    {
                        activateTask = null;
                    }
                }
            }

            //Get next task if no task is set
            if (activateTask == null)
            {
                nextTask();
            }

            //If no tasks, sleep to give time to main thread
            if (activateTask == null)
            {
                try
                {
                    sleep(100);
                }
                catch (InterruptedException e)
                {
                    ICBMClassic.logger().error(toString() + " was interrupted while sleeping",
                            e);
                }
            }
        }
    }

    /** Completes the active task and clears it, errors are logged so the thread keeps running */
    private void finishTask()
    {
        final IThreadWork task = activateTask;
        try
        {
            task.onCompleted();
            ICBMClassic.logger().debug(toString() + " complete work task " + task);
        }
        catch (Throwable t)
        {
            ICBMClassic.logger().error(toString() + " failed to complete work task " + task, t);
        }
        finally
        {
            //Clear
            activateTask = null;
        }
    }

    protected void nextTask()
    {
        activateTask = queue.poll();
        if (activateTask != null)
        {
            workCount.decrementAndGet();
            try
            {
                activateTask.onStarted();
            }
            catch (Throwable t)
            {
                ICBMClassic.logger().error(toString() + " failed to start work task " + activateTask, t);
                finishTask();
                return;
            }
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().info(toString() + " starting work task " + activateTask);
            }
        }
    }

    protected IThreadWork getCurrentTask()
    {
        return activateTask;
    }

    public void addWork(IThreadWork work)
    {
        queue.add(work);
        workCount.incrementAndGet();
    }

    public int getWorkCount()
    {
        return workCount.get();
    }

    public void stopTasks()
    {
        doRun = false;
    }
}
// PORTED
