package icbm.classic.content.entity.missile.logic;

import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.content.entity.missile.targeting.BallisticTargetingData;
import icbm.classic.lib.transform.vector.Pos;

public class TargetRangeDet {
    private final EntityExplosiveMissile missile;

    public TargetRangeDet(EntityExplosiveMissile missile) {
        this.missile = missile;
    }

    public void update() {
        if (missile.missileCapability.targetData instanceof BallisticTargetingData)
        {
            final double offset = ((BallisticTargetingData)missile.missileCapability.targetData).getImpactHeightOffset();
            if(offset > 0)
            {
                double deltaX = missile.missileCapability.targetData.getX() - missile.posX;
                double deltaY = missile.missileCapability.targetData.getY() - missile.posY;
                double deltaZ = missile.missileCapability.targetData.getZ() - missile.posZ;

                //Validate we are near flat distance of the target
                if (inRange(offset, deltaX) && inRange(offset, deltaZ))
                {
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                    if(distance <= offset)
                    {
                        missile.doExplosion();
                    }
                }
            }
        }
    }

    private boolean inRange(double range, double value)
    {
        return value <= range && value >= -range;
    }
}
