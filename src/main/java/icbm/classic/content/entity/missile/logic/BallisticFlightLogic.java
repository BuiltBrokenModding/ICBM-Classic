package icbm.classic.content.entity.missile.logic;

import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.lib.transform.vector.Pos;

import java.util.LinkedList;

public class BallisticFlightLogic
{

    // Ballistic flight vars
    public int maxHeight = 200;
    public Pos launcherPos = null;
    public double lockHeight = 3;
    public double flatDistance;
    public float missileFlightTime;
    public float acceleration;
    public boolean wasSimulated = false;

    // Ballistic flight animation
    public final int maxPreLaunchSmokeTimer = 20;
    public int preLaunchSmokeTimer = maxPreLaunchSmokeTimer;
    public LinkedList<Pos> lastSmokePos = new LinkedList<>();
    public int launcherHasAirBelow = -1;

    private final EntityExplosiveMissile missile;

    private double deltaPathX, deltaPathY, deltaPathZ;

    public BallisticFlightLogic(EntityExplosiveMissile missile)
    {
        this.missile = missile;
    }

    public void update()
    {
        // Calculate the distance difference of the missile
        this.deltaPathX = this.missile.missileCapability.targetData.getX() - this.missile.sourceOfProjectile.x();
        this.deltaPathY = this.missile.missileCapability.targetData.getY() - this.missile.sourceOfProjectile.y();
        this.deltaPathZ = this.missile.missileCapability.targetData.getZ() - this.missile.sourceOfProjectile.z();

        // TODO: Calculate parabola and relative out the targetHeight.
        // Calculate the power required to reach the target co-ordinates
        // Ground Displacement
        this.flatDistance = missile.missileCapability.targetData.calculateFlatDistance(missile.x(), missile.z());
        // Parabolic Height
        this.maxHeight = 160 + (int) (this.flatDistance * 3);
        // Flight time
        this.missileFlightTime = (float) Math.max(100, 2 * this.flatDistance) - missile.ticksInAir;
        // Acceleration
        if (!this.wasSimulated)     // only set acceleration when doing a normal launch as the missile flight time is set to -1 when it comes out of simulation.
        {
            this.acceleration = (float) this.maxHeight * 2 / (this.missileFlightTime * this.missileFlightTime);
        }
    }

}
