package icbm.classic.world.missile.logic;

import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import icbm.classic.world.missile.logic.targeting.BallisticTargetingData;
import net.minecraft.world.phys.Vec3;

public class TargetRangeDet {
    private final ExplosiveMissileEntity missile;

    public TargetRangeDet(ExplosiveMissileEntity missile) {
        this.missile = missile;
    }

    public void update() {
        if (missile.getMissileCapability().getTargetData() instanceof BallisticTargetingData) {
            final double offset = ((BallisticTargetingData) missile.getMissileCapability().getTargetData()).getImpactHeightOffset();
            if (offset > 0) {
                double deltaX = missile.getMissileCapability().getTargetData().getX() - missile.getX();
                double deltaY = missile.getMissileCapability().getTargetData().getY() - missile.getY();
                double deltaZ = missile.getMissileCapability().getTargetData().getZ() - missile.getZ();

                //Validate we are near flat distance of the target
                if (inRange(offset, deltaX) && inRange(offset, deltaZ)) // TODO account for next tick motion
                {
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                    if (distance <= offset) {
                        missile.doExplosion(new Vec3(missile.getX(), missile.getY(), missile.getZ()));
                    }
                }
            }
        }
    }

    private boolean inRange(double range, double value) {
        return value <= range && value >= -range;
    }
}
