package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileFlightLogicStep;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherBaseCapability;
import icbm.classic.content.missile.logic.flight.ArcFlightLogic;
import icbm.classic.content.missile.logic.flight.WarmupFlightLogic;
import icbm.classic.content.missile.logic.flight.move.MoveByFacingLogic;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.content.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.lib.capability.launcher.data.FiringWithDelay;
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import icbm.classic.lib.transform.rotation.EulerAngle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LauncherCapability extends LauncherBaseCapability {

    public static final int PAD_WARM_UP_TIME = 20 * 2; //TODO add config
    public static final double MISSILE_CLIMB_HEIGHT = 2; //TODO add config

    private static final EulerAngle angle = new EulerAngle(0, 0, 0);
    private static final Vec3d[] SPAWN_OFFSETS = new Vec3d[] {
        //DOWN
        new Vec3d(0, -3.1f, 0),
        //UP
        new Vec3d(0, 2.6f, 0),
        //NORTH
        new Vec3d(0, -0.2f, -2.8f),
        //SOUTH
        new Vec3d(0, -0.2f, 2.8f),
        //WEST
        new Vec3d(-2.8f, -0.2f, 0),
        //EAST
        new Vec3d(2.8f, -0.2f, 0)
    };
    private final TileLauncherBase host;

    @Override
    public IActionStatus getStatus() {
        // Min power check
        if(!host.energyStorage.consumePower(host.getFiringCost(), true)) {
            return LauncherStatus.ERROR_POWER;
        }
        // No missile stack
        else if(host.missileHolder.getMissileStack().isEmpty()) {
            return LauncherStatus.ERROR_EMPTY_STACK;
        }
        // Missile in process of firing, but is delayed
        else if(host.getFiringPackage() != null && host.getFiringPackage().getCountDown() > 0) {
            return new FiringWithDelay(host.getFiringPackage().getCountDown());
        }
        return LauncherStatus.READY;
    }

    @Override
    public IActionStatus preCheckLaunch(IMissileTarget targetData, @Nullable IMissileCause cause) {
        // Validate target data
        if(targetData == null || targetData.getPosition() == null) {
            return LauncherStatus.ERROR_TARGET_NULL;
        }
        // User safety, yes they will shoot themselves
        else if(isTargetTooClose(targetData.getPosition())) {
            return LauncherStatus.ERROR_MIN_RANGE;
        }
        // Max range TODO once fuel is added make this a warning that can be bypassed
        else if(isTargetTooFar(targetData.getPosition())) {
            return LauncherStatus.ERROR_MAX_RANGE;
        }
        //TODO if firing package countdown finishes, validate it triggered the launch... if not return QUEUED
        return getStatus();
    }

    @Override
    public IActionStatus launch(ILauncherSolution solution, @Nullable IMissileCause cause, boolean simulate) {

        final IMissileTarget targetData = solution.getTarget(this);

        // Check current status, if blocking stop launch and return
        final IActionStatus preCheck = preCheckLaunch(targetData, cause);
        if(preCheck.shouldBlockInteraction()) {
            return preCheck;
        }

        // Setup source and cause
        final BlockCause selfCause = new BlockCause(host.getWorld(), host.getPos(), host.getBlockState()); // TODO add more information about launcher
        selfCause.setPreviousCause(cause);

        final Vec3d spawnPosition = SPAWN_OFFSETS[host.getLaunchDirection().ordinal()].addVector(host.getPos().getX() + 0.5, host.getPos().getY() + 0.5, host.getPos().getZ() + 0.5);
        final MissileSource source = new MissileSource(host.getWorld(), spawnPosition, selfCause);

        //Allow canceling missile launches
        final LauncherEvent.PreLaunch event = new LauncherEvent.PreLaunch(source, this, host.missileHolder, targetData, simulate);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            if(event.cancelReason != null) {
                return event.cancelReason;
            }
            return LauncherStatus.CANCELED;
        }

        final ItemStack stack = host.missileHolder.getMissileStack();
        if (stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
        {
            final ICapabilityMissileStack missileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
            if (missileStack != null)
            {
                // TODO we may need to walk cause history to get correct launcher count info
                final Vec3d target = applyInaccuracy(targetData.getPosition(), Math.max(1, solution.getFiringCount()));

                //TODO add distance check? --- something seems to be missing

                // Ignore delay if we are currently using a firing package
                if(host.getFiringPackage() == null) {
                    // Check if we have a delay before firing
                    int delay = host.getFiringDelay();
                    if(targetData instanceof IMissileTargetDelayed) {
                        delay += ((IMissileTargetDelayed) targetData).getFiringDelay();
                    }

                    // If delay, store firing information and return
                    if(delay > 0) {
                        if(!simulate) {
                            host.setFiringPackage(new FiringPackage(targetData, cause, delay));
                        }
                        return new FiringWithDelay(delay); //TODO provide callback for when missile finishes launching
                    }
                }

                // Return launched on client or if we are simulating
                if(!getHost().isServer() || simulate) {
                    return LauncherStatus.LAUNCHED;
                }

                final IMissile missile = missileStack.newMissile(host.getWorld());
                return fireMissile(missile, source, target);
            }
        }
        return LauncherStatus.ERROR_INVALID_STACK;
    }

    private IActionStatus fireMissile(IMissile missile, MissileSource source, Vec3d target) {

        // Should always work but in rare cases capability might have failed
        if(!host.missileHolder.consumeMissile()) {
            return LauncherStatus.ERROR_INVALID_STACK;
        }

        final Entity entity = missile.getMissileEntity();
        entity.rotationPitch = entity.prevRotationPitch = host.getMissilePitch(false);
        entity.rotationYaw = entity.prevRotationYaw = host.getMissileYaw(false);

        // TODO raytrace to make sure we don't teleport through the ground
        //  raytrace for missile spawn area
        //  raytrace to check for blockage in silo path... players will be happy about this
        entity.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);

        //Trigger launch event
        missile.setTargetData(new BallisticTargetingData(target, 2)); //TODO pull from targeting data
        missile.setFlightLogic(buildFLightPath());
        missile.setMissileSource(source); //TODO encode player that built launcher, firing method (laser, remote, redstone), and other useful data
        missile.launch();

        //Spawn entity
        if(!host.getWorld().spawnEntity(entity)) {
            return LauncherStatus.ERROR_SPAWN;
        }

        // Check power again, with firing delay things could change
        if(!host.energyStorage.consumePower(host.getFiringCost(), true)) {
            return LauncherStatus.ERROR_POWER;
        }
        host.energyStorage.consumePower(host.getFiringCost(), false);

        //Grab rider
        if (host.seat != null && !host.seat.getPassengers().isEmpty()) //TODO add hook to disable riding some missiles
        {
            final List<Entity> riders = host.seat.getPassengers();
            riders.forEach(r -> {
                entity.dismountRidingEntity();
                r.startRiding(entity);
            });
        }

        return LauncherStatus.LAUNCHED;
    }

    public IMissileFlightLogic buildFLightPath() {

        if(host.getLaunchDirection() != EnumFacing.UP) {

            IMissileFlightLogicStep stepLockHeight = new MoveByFacingLogic()
                .setDistance(host.getLockHeight())
                .setRelative(false)
                .setDirection(host.getLaunchDirection())
                .setAcceleration(0.2);

            IMissileFlightLogic arcFlight = new ArcFlightLogic();

            return new WarmupFlightLogic()
                .setTimer(PAD_WARM_UP_TIME)
                .addStep(stepLockHeight)
                .addStep(arcFlight);
        }

        IMissileFlightLogicStep stepSlowClimb = new MoveByFacingLogic()
            .setDistance(MISSILE_CLIMB_HEIGHT)
            .setRelative(false)
            .setDirection(host.getLaunchDirection())
            .setAcceleration(0.005);

        IMissileFlightLogicStep stepLockHeight = new MoveByFacingLogic()
            .setDistance(host.getLockHeight() - MISSILE_CLIMB_HEIGHT)
            .setRelative(false)
            .setDirection(host.getLaunchDirection())
            .setAcceleration(0.1);

        IMissileFlightLogic arcFlight = new ArcFlightLogic();

        return new WarmupFlightLogic()
            .setTimer(PAD_WARM_UP_TIME)
            .addStep(stepSlowClimb)
            .addStep(stepLockHeight)
            .addStep(arcFlight);
    }

    @Override
    public float getInaccuracy(Vec3d target, int launcherCount) {
        // Apply inaccuracy
        float inaccuracy = (float)ConfigLauncher.MIN_INACCURACY;

        // Add inaccuracy based on range
        final double distance = host.getDistanceSq(target.x, target.y, target.z);
        final double scale = distance / (ConfigLauncher.RANGE * ConfigLauncher.RANGE);
        inaccuracy += scale * ConfigLauncher.SCALED_INACCURACY_DISTANCE;

        // Add inaccuracy for each launcher fired in circuit
        if(launcherCount > 1) {
            inaccuracy += (launcherCount - 1) * ConfigLauncher.SCALED_INACCURACY_LAUNCHERS;
        }
        return inaccuracy;
    }

    protected Vec3d applyInaccuracy(Vec3d target, int launcherCount)
    {

        //Randomize distance
        float inaccuracy = getInaccuracy(target, launcherCount) * host.getWorld().rand.nextFloat();

        //Randomize radius drop
        angle.setYaw(host.getWorld().rand.nextFloat() * 360); //TODO fix to use a normal distribution from ICBM 2

        //Apply inaccuracy to target position and return
        return new Vec3d(target.x + angle.x() * inaccuracy, target.y, target.z + angle.z() * inaccuracy);
    }

    /**
     * Checks to see if the target is too close.
     *
     * @param target
     * @return
     */
    public boolean isTargetTooClose(Vec3d target)
    {
        final int minDistance = 10; //TODO config driven and user driven
        final double deltaX = Math.abs(target.x - (host.getPos().getX() + 0.5));
        final double  deltaZ = Math.abs(target.z - (host.getPos().getZ() + 0.5));
        return deltaX < minDistance && deltaZ < minDistance;
    }

    public boolean isTargetTooFar(Vec3d target)
    {
        final double deltaX = Math.abs(target.x - (host.getPos().getX() + 0.5));
        final double  deltaZ = Math.abs(target.z - (host.getPos().getZ() + 0.5));
        return deltaX > ConfigLauncher.RANGE || deltaZ > ConfigLauncher.RANGE;
    }
}
