package icbm.classic.content.missile.logic;

import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.targeting.BallisticTargetingData;
import net.minecraft.world.phys.Vec3;

public class TargetRangeDet {
    private final EntityExplosiveMissile missile;

    public TargetRangeDet(EntityExplosiveMissile missile) {
        this.missile = missile;
    }

    public void update() {

        if(missile.level().isClientSide() || missile.getMissileCapability().getTargetData() == null) {
            return;
        }

        double offset = 0;
        if (missile.getMissileCapability().getTargetData() instanceof BallisticTargetingData) {
            offset = ((BallisticTargetingData) missile.getMissileCapability().getTargetData() ).getImpactHeightOffset();
        }
        else if(missile.explosive.getExplosiveData() == ICBMExplosives.CLUSTER) {
            offset = 10; //TODO handle as fuse settings
        }

        if(offset > 0)
        {
            double deltaX = missile.getMissileCapability().getTargetData() .getX() - missile.getX();
            double deltaY = missile.getMissileCapability().getTargetData() .getY() - missile.getY();
            double deltaZ = missile.getMissileCapability().getTargetData() .getZ() - missile.getZ();

            //Validate we are near flat distance of the target
            if (inRange(offset, deltaX) && inRange(offset, deltaZ)) // TODO account for next tick motion
            {
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                if(distance <= offset)
                {
                    missile.explosive.doExplosion(missile.getX(), missile.getY(), missile.getZ(), new ActionSource(missile.level(), new Vec3(missile.getX(), missile.getY(), missile.getZ()), new EntityCause(missile))); //TODO make lambda driven, so we can reuse fuses on other entities
                    missile.discard();
                }
            }
        }
    }

    private boolean inRange(double range, double value)
    {
        return value <= range && value >= -range;
    }
}
// PORTED
