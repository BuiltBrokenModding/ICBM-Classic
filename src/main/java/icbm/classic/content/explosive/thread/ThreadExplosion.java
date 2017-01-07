package icbm.classic.content.explosive.thread;

import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;

import java.util.HashSet;

/** @author Calclavia */
public abstract class ThreadExplosion extends Thread
{
    public final Location position;
    public int radius;
    public float energy;
    public Entity source;

    public boolean isComplete = false;

    public final HashSet<Pos> deltaSet = new HashSet<Pos>();
    public final HashSet<Pos> results = new HashSet<Pos>();

    public ThreadExplosion(Location position, int radius, float energy, Entity source)
    {
        this.position = position;
        this.radius = radius;
        this.energy = energy;
        this.source = source;
        this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run()
    {
        this.isComplete = true;
    }
}
