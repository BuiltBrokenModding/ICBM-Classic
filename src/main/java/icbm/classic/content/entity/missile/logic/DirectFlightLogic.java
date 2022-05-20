package icbm.classic.content.entity.missile.logic;

import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.content.entity.missile.EntityMissile;

/**
 * Created by Robin Seifert on 2/8/2022.
 */
public class DirectFlightLogic implements IFlightLogic<IMissileTarget>
{
    private final EntityMissile missile;

    public DirectFlightLogic(EntityMissile missile)
    {
        this.missile = missile;
    }

    @Override
    public void initializeFlight(IMissileTarget targetData)
    {
        final double deltaPathX = targetData.getX() - missile.x();
        final double deltaPathY = targetData.getY() - missile.y();
        final double deltaPathZ = targetData.getZ() - missile.z();

        missile.shoot(deltaPathX, deltaPathY, deltaPathZ, EntityMissile.DIRECT_FLIGHT_SPEED, 0);
    }

    //TODO rework to use current pitch and yaw to set motion
    //TODO update motion as long as we have fuel (ticks of motion time)

    @Override
    public <V> V predictPosition(VecBuilderFunc<V> builder, int ticks)
    {
        return builder.apply(
                missile.x() + missile.motionX * ticks,
                missile.y() + missile.motionY * ticks,
                missile.z() + missile.motionZ * ticks
        );
    }
}
