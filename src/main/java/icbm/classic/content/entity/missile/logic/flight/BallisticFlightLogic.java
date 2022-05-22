package icbm.classic.content.entity.missile.logic.flight;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.content.entity.missile.tracker.MissileTrackerHandler;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedList;

public class BallisticFlightLogic implements IMissileFlightLogic
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ballistic");

    /**
     * Ticks to animate slowly rising from the launcher
     */
    public static final int PAD_WARM_UP_TIME = 20 * 2; //TODO add config
    public static final double MISSILE_CLIMB_HEIGHT = 2; //TODO add config

    /**
     * Flag to indicate there is air blocks under the launcher
     */
    public boolean launcherHasAirBelow = false;

    public boolean hasStartedFlight = false;

    /**
     * Height Y to wait before starting arc
     */
    public double lockHeight = 0;
    /**
     * Tick runtime of flight arc
     */
    private float missileFlightTime;
    /**
     * Motion Y acceleration for arc to work
     */
    private float acceleration;


    /**
     * Timer for missile to wait on pad before climbing
     */
    private int padWarmUpTimer = PAD_WARM_UP_TIME;

    /**
     * Distance to slowly climb before starting normal path
     */
    private double climbHeight = MISSILE_CLIMB_HEIGHT;

    /**
     * Difference in distance from target, used as acceleration
     */
    private double deltaPathX, deltaPathY, deltaPathZ;
    private double startX, startY, startZ;

    private final LinkedList<Pos> clientLastSmokePos = new LinkedList<>();

    @Override
    public void calculateFlightPath(final World world, double startX, double startY, double startZ, final IMissileTarget targetData)
    {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;

        //Setup arc data
        calculatePath(startX, startY, startZ, targetData);

        //Check if we have air under the launcher, used for animation smoke during launch
        final BlockPos blockUnderLauncher = new BlockPos(
            Math.signum(startX) * Math.floor(Math.abs(startX)),
            startY - 2, //TODO is this checking the correct block?
            Math.signum(startZ) * Math.floor(Math.abs(startZ))
        );
        this.launcherHasAirBelow = world.isAirBlock(blockUnderLauncher);
    }

    protected void calculatePath(double startX, double startY, double startZ, final IMissileTarget targetingData)
    {
        if (targetingData != null)
        {
            // Calculate the distance difference of the missile
            this.deltaPathX = targetingData.getX() - startX;
            this.deltaPathY = targetingData.getY() - startY;
            this.deltaPathZ = targetingData.getZ() - startZ;

            // TODO: Calculate parabola and relative out the targetHeight.
            // Calculate the power required to reach the target co-ordinates
            // Ground Displacement
            double flatDistance = targetingData.calculateFlatDistance(startX, startZ);
            // Parabolic Height
            // Ballistic flight vars
            //TODO make config?
            int arcHeightMax = 160 + (int) (flatDistance * 3);
            // Flight time
            missileFlightTime = (float) Math.max(100, 2 * flatDistance);
            // Acceleration
            this.acceleration = (float) arcHeightMax * 2 / (missileFlightTime * missileFlightTime);

            //TODO test impact position, as this may be offset by lockHeight and deltaPathY causing it to miss slightly
            //  In theory missile should be moving almost strait down on impact but could be a problem in some cases
        } else
        {
            acceleration = ConfigMissile.DIRECT_FLIGHT_SPEED;
        }
    }

    @Override
    public void onEntityTick(Entity entity, int ticksInAir)
    {
        //Warm up on pad for nice animation
        if (padWarmUpTimer > 0)
        {
            padWarmUpTimer--;
            idleMissileOnPad(entity, ticksInAir);
        }
        //Slowly climb out of the launcher TODO get climb height from launcher
        else if (climbHeight > 0)
        {
            doSlowClimb(entity, ticksInAir);
        }
        //Starts the missile into normal flight
        else if (!hasStartedFlight) {
            hasStartedFlight = true;
            entity.motionY = this.acceleration * (missileFlightTime / 2);
            entity.motionX = this.deltaPathX / missileFlightTime;
            entity.motionZ = this.deltaPathZ / missileFlightTime;
        }
        //Normal path logic
        else {
            runFlightLogic(entity, ticksInAir);
        }

        ICBMClassic.logger().info(String.format("Missile: %.2f, %.2f, %.2f, %.2f, %.2f", entity.motionX, entity.motionY, entity.motionZ, climbHeight, lockHeight));
    }

    protected void runFlightLogic(Entity entity, int ticksInAir)
    {
        if (!entity.world.isRemote)
        {
            //Move up if we are still in lock height
            if (this.lockHeight > 0)
            {
                handleLockHeight(entity, ticksInAir);
            } else
            {
                //Apply arc acceleration logic
                entity.motionY -= this.acceleration;
                alignWithMotion(entity);
            }

            if (entity instanceof EntityMissile && shouldSimulate(entity))
            {
                MissileTrackerHandler.simulateMissile((EntityExplosiveMissile) entity); //TODO add ability to simulate any entity
            }
        }
    }

    protected void handleLockHeight(Entity entity, int ticksInAir)
    {
        entity.motionY = ConfigMissile.LAUNCH_SPEED * ticksInAir * (ticksInAir / 2f);
        entity.motionX = 0;
        entity.motionZ = 0;
        this.lockHeight -= entity.motionY; //TODO fix to account for slow animation climb
    }

    protected void alignWithMotion(Entity entity)
    {
        entity.rotationPitch = (float) (Math.atan(entity.motionY / (Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ))) * 180 / Math.PI);
        // Look at the next point
        entity.rotationYaw = (float) (Math.atan2(entity.motionX, entity.motionZ) * 180 / Math.PI);
    }

    /**
     * Has the missile slow climb up before start full motion
     *
     * @param entity     representing the missile
     * @param ticksInAir the missile has been in the air
     */
    protected void doSlowClimb(Entity entity, int ticksInAir)
    {
        entity.motionY += 0.005f; //TODO add config
        lockHeight -= entity.motionY;
        climbHeight -= entity.motionY;
    }

    /**
     * Has the missile wait on the pad while it's engines start and generate a lot of smoke
     *
     * @param entity     representing the missile
     * @param ticksInAir the missile has been in the air
     */
    protected void idleMissileOnPad(Entity entity, int ticksInAir)
    {
        entity.prevRotationPitch = entity.rotationPitch = 90;
        //ICBMClassic.proxy.spawnMissileSmoke(entity, this, ticksInAir); TODO implement custom smoke logic that generates small cloud at base of launcher
    }

    @Override
    public boolean shouldRunEngineEffects(Entity entity) {
        return padWarmUpTimer <= 0;
    }

    protected boolean shouldSimulate(Entity entity)
    {
        if (entity.getPassengers().stream().anyMatch(rider -> rider instanceof EntityPlayerMP))
        {
            return false;
        } else if (entity.posY >= ConfigMissile.SIMULATION_START_HEIGHT)
        {
            return true;
        }

        //About to enter an unloaded chunk
        return !entity.world.isBlockLoaded(predictPosition(entity, BlockPos::new, 1));
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;

        double motionY = entity.motionY;

        while (ticks-- > 0)
        {
            motionY -= this.acceleration;

            x += entity.motionX;
            y += motionY;
            z += entity.motionZ;
        }

        return builder.apply(x, y, z);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    @Deprecated
    public int getPadWarmUpTimer()
    {
        return padWarmUpTimer;
    }

    public LinkedList<Pos> getLastSmokePos()
    {
        return clientLastSmokePos;
    }

    @Override
    public NBTTagCompound save()
    {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        //Disable gravity and friction
        return false;
    }

    private static final NbtSaveHandler<BallisticFlightLogic> SAVE_LOGIC = new NbtSaveHandler<BallisticFlightLogic>()
        //Stuck in ground data
        .addRoot("flags")
        /* */.nodeBoolean("air_under", (bl) -> bl.launcherHasAirBelow, (bl, data) -> bl.launcherHasAirBelow = data)
        /* */.nodeBoolean("flight_started", (bl) -> bl.hasStartedFlight, (bl, data) -> bl.hasStartedFlight = data)
        .base()
        .addRoot("inputs")
        /* */.nodeDouble("start_x", (bl) -> bl.startX, (bl, i) -> bl.startX = i)
        /* */.nodeDouble("start_y", (bl) -> bl.startY, (bl, i) -> bl.startY = i)
        /* */.nodeDouble("start_z", (bl) -> bl.startZ, (bl, i) -> bl.startZ = i)
        .base()
        .addRoot("calculated")
        /* */.nodeFloat("flight_time", (bl) -> bl.missileFlightTime, (bl, data) -> bl.missileFlightTime = data)
        /* */.nodeFloat("acceleration", (bl) -> bl.acceleration, (bl, data) -> bl.acceleration = data)
        /* */.nodeDouble("delta_x", (bl) -> bl.deltaPathX, (bl, data) -> bl.deltaPathX = data)
        /* */.nodeDouble("delta_y", (bl) -> bl.deltaPathY, (bl, data) -> bl.deltaPathY = data)
        /* */.nodeDouble("delta_z", (bl) -> bl.deltaPathZ, (bl, data) -> bl.deltaPathZ = data)
        .base()
        .addRoot("timers")
        /* */.nodeInteger("engine_warm_up", (bl) -> bl.padWarmUpTimer, (bl, data) -> bl.padWarmUpTimer = data)
        /* */.nodeDouble("climb_height", (bl) -> bl.climbHeight, (bl, data) -> bl.climbHeight = data)
        /* */.nodeDouble("lock_height", (bl) -> bl.lockHeight, (bl, i) -> bl.lockHeight = i)
        .base();

}
