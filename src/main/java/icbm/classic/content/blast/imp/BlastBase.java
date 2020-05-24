package icbm.classic.content.blast.imp;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public abstract class BlastBase implements IBlastInit
{
    private World world;
    private double x, y, z;
    private boolean locked;

    @Nonnull
    @Override
    public BlastState runBlast()
    {
        if(world != null) {
            final EntityRedmatter entityRedmatter = new EntityRedmatter(world);
            entityRedmatter.posX = x();
            entityRedmatter.posY = y();
            entityRedmatter.posZ = z();
            world.spawnEntity(entityRedmatter);
        }
        return BlastState.ERROR;
    }

    @Override
    public void clearBlast()
    {

    }

    //<editor-fold desc="pos-data">
    @Override
    public World world()
    {
        return null;
    }

    @Override
    public double z()
    {
        return z;
    }

    @Override
    public double x()
    {
        return x;
    }

    @Override
    public double y()
    {
        return y;
    }
    //</editor-fold>

    //<editor-fold desc="blast-init">
    @Override
    public IBlastInit setBlastWorld(World world)
    {
        if(!locked)
        {
            this.world = world;
        }
        return this;
    }

    @Override
    public IBlastInit setBlastPosition(double x, double y, double z)
    {
        if(!locked)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        return this;
    }

    @Override
    public IBlastInit buildBlast()
    {
        locked = true;
        return this;
    }
    //</editor-fold>
}
