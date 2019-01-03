package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public class BlastEvent<B extends IBlast> extends Event
{
    /** Source of the event */
    public final IBlast blast;

    public BlastEvent(IBlast blast)
    {
        this.blast = blast;
    }

    /**
     * Source of the blast.
     */
    public World world()
    {
        return blast.world();
    }

    /**
     * Source of the blast.
     */
    public double x()
    {
        return blast.x();
    }

    /**
     * Source of the blast.
     */
    public double y()
    {
        return blast.y();
    }

    /**
     * Source of the blast.
     */
    public double z()
    {
        return blast.z();
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
        return blast.getBlastSource();
    }
}
