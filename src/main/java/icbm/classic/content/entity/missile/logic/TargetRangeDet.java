package icbm.classic.content.entity.missile.logic;

import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.lib.transform.vector.Pos;

public class TargetRangeDet {
    private final EntityExplosiveMissile missile;

    public TargetRangeDet(EntityExplosiveMissile missile) {
        this.missile = missile;
    }

    public void update() {
        if (missile.ballisticFlightLogic.targetPos != null && missile.ballisticFlightLogic.targetHeight >= 0)
        {
            int deltaX = missile.ballisticFlightLogic.targetPos.xi() - (int) Math.floor(missile.posX);
            int deltaY = missile.ballisticFlightLogic.targetPos.yi() - (int) Math.floor(missile.posY);
            int deltaZ = missile.ballisticFlightLogic.targetPos.zi() - (int) Math.floor(missile.posZ);

            if (inRange(1, deltaY) && inRange(1, deltaX) && inRange(1, deltaZ))
            {
                missile.doExplosion();
            }
        }
    }

    private boolean inRange(int range, int value)
    {
        return value <= range && value >= -range;
    }
}
