package icbm.classic.content.blast.thread;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/** @author Calclavia */
@Deprecated
public abstract class ThreadExplosion extends Thread //TODO replace with worker threads, best not to spawn a thread per blast
{
    public final Blast blast;
    public final Location position;
    public int radius;
    public float energy;
    public Entity source;

    /** Is the thread completed */
    public boolean isComplete = false;

    /** Used to kill the thread on its next loop cycle */
    protected boolean kill = false;

    public ThreadExplosion(Blast blast, int radius, float energy, Entity source)
    {
        super(null, null, "ThreadExplosion-" + nextThreadID(), 0);
        this.blast = blast;
        this.position = blast.location;
        this.radius = radius;
        this.energy = energy;
        this.source = source;
        this.setPriority(Thread.MIN_PRIORITY); //We don't care how fast this runs
        this.setDaemon(true); //Fix for threads still running when MC closes
    }


    private static int threadIDNumber;
    private static synchronized int nextThreadID() {
        return threadIDNumber++;
    }

    @Override
    public void interrupt()
    {
        if (ConfigDebug.DEBUG_THREADS)
        {
            String msg = String.format("ThreadExplosion#interrupt() \nBlast = %s\nPosition = %s\nRadius = %s",
                    blast != null ? blast : "null", blast != null ? position : "null", radius);
            ICBMClassic.logger().error(msg,
                    new RuntimeException("Trace"));
        }
        super.interrupt();
    }

    @Override
    public final void run()
    {
        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info(String.format("ThreadExplosion#run() -> start \nBlast = %s\nPosition = %s\nRadius = %s", blast, position, radius));
        }

        //Normal run
        try
        {
            if (position != null && position.world() != null)
            {
                doRun(position.world, position);
            }
            else
            {
                ICBMClassic.logger().error("ThreadExplosion#run() -> Invalid world or position provided for thread. " +
                        "Canceling action to prevent issues. \n Pos = " + position + " World = " + (position != null ? position.world : "null"));
            }
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("ThreadExplosion#run() -> Unexpected error ", e);
        }

        //Marked as completed
        this.isComplete = true;
        this.blast.markThreadCompleted(this);

        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info(String.format("ThreadExplosion#run() -> end \nBlast = %s\nPosition = %s\nRadius = %s", blast, position, radius));
        }
    }

    public void kill()
    {
        kill = true;

        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info(String.format("ThreadExplosion#kill() \nBlast = %s\nPosition = %s\nRadius = %s", blast, position, radius));
        }
    }

    protected abstract void doRun(World world, Location center);
}
