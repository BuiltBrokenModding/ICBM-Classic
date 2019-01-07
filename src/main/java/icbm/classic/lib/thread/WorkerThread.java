package icbm.classic.lib.thread;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigDebug;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public class WorkerThread extends Thread
{
    public boolean doRun = true;

    private IThreadWork activateTask;
    private final ConcurrentLinkedQueue<IThreadWork> queue = new ConcurrentLinkedQueue();
    private int workCount = 0;

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
                //Run step
                if (!activateTask.doRun(1))
                {
                    //Complete
                    activateTask.onCompleted();
                    ICBMClassic.logger().debug(toString() + " complete work task " + activateTask);

                    //Clear
                    activateTask = null;
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

    protected void nextTask()
    {
        activateTask = queue.poll();
        if (activateTask != null)
        {
            workCount--;
            activateTask.onStarted();
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
        workCount++;
    }

    public int getWorkCount()
    {
        return workCount;
    }

    public void stopTasks()
    {
        doRun = false;
    }
}
