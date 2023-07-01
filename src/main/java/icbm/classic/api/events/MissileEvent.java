package icbm.classic.api.events;

import icbm.classic.api.missiles.IMissile;
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
     * <p>
     * Called after {@link LauncherEvent.PreLaunch}
     */
    public static class PostLaunch extends MissileEvent
    {
        public PostLaunch(IMissile missile, Entity entityMissile)
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
     * <p>
     * Called before {@link PostImpact}
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

    /**
     * Called when the missile is about to enter the simulation queue. Use
     * this to prevent simulation or capture the missile to send it to a different
     * queue or system.
     *
     * Main purpose of this event is to block simulation. It does offer the option
     * to change how simulation works or switch queues. The problem with this is
     * cross dimension should be handled by other mechanics. As the missile may
     * not be able to predict this behavior correctly. Resulting in strange
     * interactions and broken expectations of the player.
     *
     * Instead, modify the flight/guidance system of the missile. Allowing it to
     * deliberately switching dimensions and properly enter the other dimension as expected.
     * Such as entering the bottom of a space dimension or orbit of a planet.
     *
     * For magic or disconnected dimensions please use a portal. This can easily be implemented
     * on the portal's side or the block impact system of the missile.
     */
    @Cancelable
    public static class EnteringSimQueue extends MissileEvent
    {
        public EnteringSimQueue(IMissile missile, Entity entityMissile)
        {
            super(missile, entityMissile);
        }
    }

    /**
     * Called right after the missile ran its impact code.
     * <p>
     * Called after {@link PreImpact}
     */
    public static class PostImpact extends MissileEvent
    {
        public final RayTraceResult hit;

        public PostImpact(IMissile missile, Entity entityMissile, RayTraceResult hit)
        {
            super(missile, entityMissile);
            this.hit = hit;
        }
    }
}
