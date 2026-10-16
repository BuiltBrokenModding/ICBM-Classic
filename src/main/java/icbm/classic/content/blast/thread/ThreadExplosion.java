package icbm.classic.content.blast.thread;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** @author Calclavia */
@Deprecated
public abstract class ThreadExplosion extends Thread //TODO replace with worker threads, best not to spawn a thread per blast
{
    public final Blast blast;
    public final Location position;
    public int radius;
    public float energy;
    public Entity source;

    /**
     * PORT: loaded chunks around the blast, captured on the server thread when the thread is created. The level
     * can not be read from this thread in 1.21 without blocking on the server thread, see {@link ChunkSnapshot}.
     */
    protected final ChunkSnapshot snapshot;

    /** Is the thread completed, set by this thread and read by the server thread */
    public volatile boolean isComplete = false;

    /** Used to kill the thread on its next loop cycle, set by the server thread */
    protected volatile boolean kill = false;

    public ThreadExplosion(Blast blast, int radius, float energy, Entity source)
    {
        super(null, null, "ThreadExplosion-" + nextThreadID(), 0);
        this.blast = blast;
        this.position = blast.location;
        this.radius = radius;
        this.energy = energy;
        this.source = source;
        // Small explosion rays travel up to about 1.75x the radius (power 1.3 * radius, 0.3 blocks per 0.225 power)
        this.snapshot = ChunkSnapshot.capture(blast.world(), position != null ? position.x() : 0, position != null ? position.z() : 0,
            (int) Math.ceil(radius * 1.75F) + 2);
        // Used for resistance checks, created here so the thread does not create it lazily
        if (blast.world() != null && position != null)
        {
            blast.asVanillaExplosion();
        }
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
                        "Canceling action to prevent issues. \n Pos = " + position + " Level = " + (position != null ? position.world : "null"));
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

    protected abstract void doRun(Level world, Location center);
}
// PORTED
