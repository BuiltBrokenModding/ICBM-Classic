package icbm.classic.content.entity.missile.logic;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.content.entity.missile.targeting.BallisticTargetingData;
import icbm.classic.content.entity.missile.tracker.MissileTrackerHandler;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.LinkedList;

public class BallisticFlightLogic implements IFlightLogic<BallisticTargetingData>, INBTSerializable<NBTTagCompound>
{
    /**
     * Ticks to animate slowly rising from the launcher
     */
    public final int MAX_PRE_LAUNCH_SMOKE_TICKS = 20; //TODO add config

    /**
     * Has missile been simulated off map already
     */
    private boolean wasSimulated = false;

    /**
     * Flag to indicate there is air blocks under the launcher
     */
    public boolean launcherHasAirBelow = false;

    /**
     * Height Y to wait before starting arc
     */
    public double lockHeight = 3;
    /**
     * Tick runtime of flight arc
     */
    private float missileFlightTime;
    /**
     * Motion Y acceleration for arc to work
     */
    private float acceleration;


    /**
     * Timer for launching animation
     */
    private int preLaunchSmokeTimer = 0;

    /**
     * Actual missile
     */
    private final EntityExplosiveMissile missile;

    /**
     * Difference in distance from target, used as acceleration
     */
    private double deltaPathX, deltaPathY, deltaPathZ;

    private final LinkedList<Pos> clientLastSmokePos = new LinkedList<>();

    public BallisticFlightLogic(EntityExplosiveMissile missile)
    {
        this.missile = missile;
    }

    @Override
    public void initializeFlight(final BallisticTargetingData targetData)
    {
        this.preLaunchSmokeTimer = MAX_PRE_LAUNCH_SMOKE_TICKS;

        //Setup arc data
        calculatePath(targetData);

        //Check if we have air under the launcher, used for animation smoke during launch
        final BlockPos blockUnderLauncher = new BlockPos(
            Math.signum(this.missile.posX) * Math.floor(Math.abs(this.missile.posX)),
            this.missile.posY - 2, //TODO is this checking the correct block?
            Math.signum(this.missile.posZ) * Math.floor(Math.abs(this.missile.posZ))
        );
        this.launcherHasAirBelow = missile.world.isAirBlock(blockUnderLauncher);
    }

    protected void calculatePath(final BallisticTargetingData targetingData)
    {
        // Calculate the distance difference of the missile
        this.deltaPathX = targetingData.getX() - this.missile.sourceOfProjectile.x();
        this.deltaPathY = targetingData.getY() - this.missile.sourceOfProjectile.y();
        this.deltaPathZ = targetingData.getZ() - this.missile.sourceOfProjectile.z();

        // TODO: Calculate parabola and relative out the targetHeight.
        // Calculate the power required to reach the target co-ordinates
        // Ground Displacement
        double flatDistance = targetingData.calculateFlatDistance(missile.x(), missile.z());
        // Parabolic Height
        // Ballistic flight vars
        //TODO make config?
        int arcHeightMax = 160 + (int) (flatDistance * 3);
        // Flight time
        this.missileFlightTime = (float) Math.max(100, 2 * flatDistance) - missile.ticksInAir;
        // Acceleration
        if (!this.wasSimulated())     // only set acceleration when doing a normal launch as the missile flight time is set to -1 when it comes out of simulation.
        {
            this.acceleration = (float) arcHeightMax * 2 / (this.missileFlightTime * this.missileFlightTime);
        }
    }

    @Override
    public void onEntityTick()
    {
        if (!this.missile.world.isRemote)
        {
            runServerLogic();
        }

        if (getPreLaunchSmokeTimer() > 0)
        {
            preLaunchSmokeTimer = getPreLaunchSmokeTimer() - 1;
        }
    }

    protected void runServerLogic()
    {
        if (getPreLaunchSmokeTimer() <= 0)
        {
            //Move up if we are still in lock height
            if (this.lockHeight > 0)
            {
                handleLockHeight();
            } else
            {
                //Apply arc acceleration logic
                this.missile.motionY -= this.acceleration;

                alignWithMotion();
            }

            if (shouldSimulate())
            {
                MissileTrackerHandler.simulateMissile(this.missile);
            }
        } else
        {
            handleSlowAnimationClimb();
        }
    }

    protected void handleLockHeight()
    {
        this.missile.motionY = ConfigMissile.LAUNCH_SPEED * this.missile.ticksInAir * (this.missile.ticksInAir / 2f);
        this.missile.motionX = 0;
        this.missile.motionZ = 0;
        this.lockHeight -= this.missile.motionY; //TODO fix to account for slow animation climb
        if (this.lockHeight <= 0)
        {
            this.missile.motionY = this.acceleration * (this.missileFlightTime / 2); //TODO this doesn't match init alg
            this.missile.motionX = this.deltaPathX / missileFlightTime;
            this.missile.motionZ = this.deltaPathZ / missileFlightTime;
        }
    }

    protected void alignWithMotion()
    {
        this.missile.rotationPitch = (float) (Math.atan(this.missile.motionY / (Math.sqrt(this.missile.motionX * this.missile.motionX + this.missile.motionZ * this.missile.motionZ))) * 180 / Math.PI);
        // Look at the next point
        this.missile.rotationYaw = (float) (Math.atan2(this.missile.motionX, this.missile.motionZ) * 180 / Math.PI);
    }

    protected void handleSlowAnimationClimb()
    {
        this.missile.motionY = 0.001f;
        this.lockHeight -= this.missile.motionY; //TODO not sure why we are updating motion when we are forcing position as well

        this.missile.posY = this.missile.sourceOfProjectile.y() + 2.2f; //TODO why 2.2f?
        this.missile.prevRotationPitch = 90f;
        this.missile.rotationPitch = 90f;
        ICBMClassic.proxy.spawnMissileSmoke(this.missile);
    }

    protected boolean shouldSimulate()
    {
        //TODO predict position, if traveling into unloaded chunk simulate

        if (this.missile.sourceOfProjectile != null)
        {
            if (this.missile.getPassengers().stream().anyMatch(entity -> entity instanceof EntityPlayerMP))
            {
                return false;
            } else if (wasSimulated())
            {
                return false;
            } else if (this.missile.posY >= ConfigMissile.SIMULATION_START_HEIGHT)
            {
                return true;
            }

            //About to enter an unloaded chunk
            return !this.missile.world.isBlockLoaded(predictPosition(BlockPos::new, 1));
        }
        return false;
    }

    @Override
    public <V> V predictPosition(VecBuilderFunc<V> builder, int ticks)
    {
        double x = this.missile.posX;
        double y = this.missile.posY;
        double z = this.missile.posZ;

        double motionY = this.missile.motionY;

        while (ticks-- > 0)
        {
            motionY -= this.acceleration;

            x += this.missile.motionX;
            y += motionY;
            z += this.missile.motionZ;
        }

        return builder.apply(x, y, z);
    }

    public boolean wasSimulated()
    {
        return wasSimulated;
    }

    public void markSimulationCompleted()
    {
        this.wasSimulated = true;
    }

    public int getPreLaunchSmokeTimer()
    {
        return preLaunchSmokeTimer;
    }

    public LinkedList<Pos> getLastSmokePos()
    {
        return clientLastSmokePos;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<BallisticFlightLogic> SAVE_LOGIC = new NbtSaveHandler<BallisticFlightLogic>()
        //Stuck in ground data
        .addRoot("flags")
        /* */.nodeBoolean("air_under", (bl) -> bl.launcherHasAirBelow, (bl, data) -> bl.launcherHasAirBelow = data)
        /* */.nodeBoolean("was_simulate", (bl) -> bl.wasSimulated, (bl, data) -> bl.wasSimulated = data)
        .base()
        .addRoot("calculated")
        /* */.nodeFloat("flight_time", (bl) -> bl.missileFlightTime, (bl, data) -> bl.missileFlightTime = data)
        /* */.nodeFloat("acceleration", (bl) -> bl.acceleration, (bl, data) -> bl.acceleration = data)
        /* */.nodeDouble("delta_x", (bl) -> bl.deltaPathX, (bl, data) -> bl.deltaPathX = data)
        /* */.nodeDouble("delta_y", (bl) -> bl.deltaPathY, (bl, data) -> bl.deltaPathY = data)
        /* */.nodeDouble("delta_z", (bl) -> bl.deltaPathZ, (bl, data) -> bl.deltaPathZ = data)
        .base()
        .addRoot("ticks")
        /* */.nodeInteger("pre_launch", (bl) -> bl.preLaunchSmokeTimer, (bl, data) -> bl.preLaunchSmokeTimer = data)
        .base();

}
