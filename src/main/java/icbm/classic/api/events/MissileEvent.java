package icbm.classic.api.events;

import icbm.classic.api.caps.IMissile;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public abstract class MissileEvent extends Event
{

    public final IMissile missile;
    public final Entity entityMissile;

    public MissileEvent(IMissile missile, Entity entityMissile)
    {
        this.missile = missile;
        this.entityMissile = entityMissile;
    }

    /**
     * Called right after the missile sets up its launch
     * data but before it is set into motion. Use this
     * to change settings, target, or trigger additional
     * logic.
     */
    public static class OnLaunched extends MissileEvent
    {

        public OnLaunched(IMissile missile, Entity entityMissile)
        {
            super(missile, entityMissile);
        }
    }

    /**
     * Called right before the missile runs its impact code. Use
     * this to cancel impact for special use cases. Such as
     * having the missile pass through blocks, ignore friendly fire,
     * or trigger a different explosion/result.
     * <p>
     * This is normally called from the ray trace impact
     * function. The missile may be a tick away from the
     * actual impact point. Keep this in mind when using
     * the missile's position vs impact position from the
     * ray hit.
     */
    @Cancelable
    public static class PreImpact extends MissileEvent
    {

        public final RayTraceResult hit;

        public PreImpact(IMissile missile, Entity entityMissile, RayTraceResult hit)
        {
            super(missile, entityMissile);
            this.hit = hit;
        }
    }
}
