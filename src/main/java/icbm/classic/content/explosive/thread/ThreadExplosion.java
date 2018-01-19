package icbm.classic.content.explosive.thread;

import com.builtbroken.mc.imp.transform.vector.Location;
import icbm.classic.content.explosive.blast.Blast;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** @author Calclavia */
public abstract class ThreadExplosion extends Thread
{
    public final Blast blast;
    public final Location position;
    public int radius;
    public float energy;
    public Entity source;

    public boolean isComplete = false;

    public final List<BlockPos> results = new ArrayList();

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
    public void run()
    {
        this.isComplete = true;
    }
}
