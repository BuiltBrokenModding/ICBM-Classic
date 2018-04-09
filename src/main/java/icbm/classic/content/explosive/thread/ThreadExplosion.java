package icbm.classic.content.explosive.thread;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/** @author Calclavia */
public abstract class ThreadExplosion extends Thread
{
    public final Blast blast;
    public final Location position;
    public int radius;
    public float energy;
    public Entity source;

    public boolean isComplete = false;
    public boolean kill = false;

    public ThreadExplosion(Blast blast, int radius, float energy, Entity source)
    {
        this.blast = blast;
        this.position = blast.position;
        this.radius = radius;
        this.energy = energy;
        this.source = source;
        this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public final void run()
    {
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
            ICBMClassic.logger().error("ThreadExplosion#run() -> Unexpected error ");
        }
        this.isComplete = true;
    }

    public void kill()
    {
        kill = true;
    }

    protected abstract void doRun(World world, Location center);
}
