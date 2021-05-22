package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.responses.BlastResponse;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper for exposing the {@link EntityRedmatter} as a Blast
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class BlastRedmatterWrapper implements IBlast
{
    private final EntityRedmatter host;

    public BlastRedmatterWrapper(EntityRedmatter host)
    {
        this.host = host;
    }

    @Nonnull
    @Override
    public BlastResponse runBlast()
    {
        return BlastState.ALREADY_TRIGGERED.genericResponse;
    }

    @Override
    public void clearBlast()
    {
        host.setDead();
    }

    //<editor-fold desc="properties">
    @Override
    public boolean isCompleted()
    {
        return host.isDead;
    }

    @Override
    @Nullable
    public Entity getEntity()
    {
        return host;
    }

    @Override
    @Nullable
    public Entity getBlastSource()
    {
        return host;
    }
    //</editor-fold>

    //<editor-fold desc="position-data">
    @Override
    public World world()
    {
        return host.world;
    }

    @Override
    public double z()
    {
        return host.posZ;
    }

    @Override
    public double x()
    {
        return host.posX;
    }

    @Override
    public double y()
    {
        return host.posY;
    }
    //</editor-fold>
}
