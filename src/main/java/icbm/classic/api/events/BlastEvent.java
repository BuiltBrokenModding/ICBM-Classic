package icbm.classic.api.events;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.explosion.IBlast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/3/19.
 *
 * @deprecated will be replaced with an event providing {@link icbm.classic.api.actions.IAction}
 */
public abstract class BlastEvent<B extends IBlast> extends Event
{
    /** Source of the event */
    public final IAction blast;

    public BlastEvent(IAction blast)
    {
        this.blast = blast;
    }

    /**
     * Source of the blast.
     */
    public Level world()
    {
        return blast.getWorld();
    }

    /**
     * Source of the blast.
     */
    public double x()
    {
        return blast.getPosition().x;
    }

    /**
     * Source of the blast.
     */
    public double y()
    {
        return blast.getPosition().y;
    }

    /**
     * Source of the blast.
     */
    public double z()
    {
        return blast.getPosition().z;
    }

    /**
     * Source of the blast.
     * <p>
     * Normally a Missile, Grenade, or Minecraft
     *
     * @return entity, can be null in some cases
     */
    public Entity getSourceEntity()
    {
        if(blast instanceof IBlast) {
            return ((IBlast) blast).getBlastSource();
        }
        return null;
    }
}
// PORTED
