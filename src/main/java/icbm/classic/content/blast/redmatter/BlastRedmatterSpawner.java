package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class BlastRedmatterSpawner implements IBlast
{
    @Override
    public float getBlastRadius()
    {
        return 0;
    }

    @Override
    public Entity getBlastSource()
    {
        return null;
    }

    @Override
    public BlastState runBlast()
    {
        return null;
    }

    @Override
    public boolean isCompleted()
    {
        return false;
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity()
    {
        return null;
    }

    @Override
    public void clearBlast()
    {

    }

    @Override
    public World world()
    {
        return null;
    }

    @Override
    public double z()
    {
        return 0;
    }

    @Override
    public double x()
    {
        return 0;
    }

    @Override
    public double y()
    {
        return 0;
    }
    //TODO create a blast that spawns the redmatter
}
