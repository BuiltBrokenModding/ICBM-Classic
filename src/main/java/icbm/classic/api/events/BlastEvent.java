package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public abstract class BlastEvent<B extends IBlast> extends Event {
    /**
     * Source of the event
     */
    public final B blast;

    public BlastEvent(B blast) {
        this.blast = blast;
    }

    /**
     * Source of the blast.
     */
    public Level level() {
        return blast.level();
    }

    /**
     * Source of the blast.
     */
    public double x() {
        return blast.x();
    }

    /**
     * Source of the blast.
     */
    public double y() {
        return blast.y();
    }

    /**
     * Source of the blast.
     */
    public double z() {
        return blast.z();
    }

    /**
     * Source of the blast.
     * <p>
     * Normally a Missile, Grenade, or Minecraft
     *
     * @return entity, can be null in some cases
     */
    public Entity getSourceEntity() {
        return blast.getBlastSource();
    }
}
