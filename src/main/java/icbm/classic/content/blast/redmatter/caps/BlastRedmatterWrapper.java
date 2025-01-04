package icbm.classic.content.blast.redmatter.caps;

import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.status.ActionResponses;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper for exposing the {@link EntityRedmatter} as a Blast
 *
 * Created by Dark(DarkGuardsman, Robin) on 4/19/2020.
 */
@Deprecated //TODO rework so we don't need a blast wrapper as this is an entity that should be spawned using IAction not IBlast
public class BlastRedmatterWrapper implements IBlast
{
    private final EntityRedmatter host;

    public BlastRedmatterWrapper(EntityRedmatter host)
    {
        this.host = host;
    }

    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        return ActionResponses.COMPLETED;
    }

    @Nonnull
    @Override
    public IActionSource getSource() {
        return new ActionSource(DimensionType.getKey(host.dimension), new Vec3d(host.posX, host.posY, host.posZ), new EntityCause(host));
    }

    @Override
    public void clearBlast()
    {
        host.remove();
    }

    //<editor-fold desc="properties">
    @Override
    public boolean isCompleted()
    {
        return !host.isAlive();
    }

    @Nonnull
    @Override
    public IExplosiveData getActionData() {
        return ICBMExplosives.REDMATTER;
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
