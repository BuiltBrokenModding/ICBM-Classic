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

    public static class Launch extends MissileEvent
    {

        public Launch(IMissile missile, Entity entityMissile)
        {
            super(missile, entityMissile);
        }
    }

    @Cancelable
    public static class Impact extends MissileEvent
    {
        public final RayTraceResult hit;

        public Impact(IMissile missile, Entity entityMissile, RayTraceResult hit)
        {
            super(missile, entityMissile);
            this.hit = hit;
        }
    }
}
