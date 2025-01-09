package icbm.classic.content.blast.imp;

import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.actions.status.ActionResponses;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robin) on 4/19/2020.
 */
public abstract class BlastBase implements IBlastInit
{
    private World world;
    private double x, y, z;
    private boolean locked;

    @Deprecated //TODO remove from base, not all explosives have a size
    private double blastSize;

    private IExplosiveData explosiveData;
    private IActionSource actionSource;

    protected abstract IActionStatus triggerBlast();



    @Override
    public void clearBlast()
    {

    }

    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        final World world = getWorld();
        if (world != null)
        {
            if(!world.isRemote)
            {
                return triggerBlast();
            }
            return ActionResponses.COMPLETED;
        }
        return ActionResponses.MISSING_WORLD;
    }

    @Override
    public float getBlastRadius()
    {
        return (float) Math.min(blastSize, 1);
    }

    @Override
    @Nonnull
    public IActionSource getSource() {
        return this.actionSource;
    }

    /**
     * Gets data used to create this action
     *
     * @return data
     */
    @Override
    @Nonnull
    public IExplosiveData getActionData() {
        return explosiveData;
    }

    @Override
    public Vec3d getPosition() {
        return new Vec3d(x, y, z);
    }

    @Override
    public World getWorld() {
        return world;
    }

    //</editor-fold>

    //<editor-fold desc="blast-init">

    @Override
    public IBlastInit setBlastSize(double size) {
        if(!locked) {
            this.blastSize = size;
        }
        return this;
    }

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
    public IBlastInit setActionSource(IActionSource source) {
        if(!locked) {
            this.actionSource = source;
        }
        return this;
    }

    @Override
    public IBlastInit setExplosiveData(IExplosiveData data)
    {
        if(!locked) {
            this.explosiveData = data;
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
