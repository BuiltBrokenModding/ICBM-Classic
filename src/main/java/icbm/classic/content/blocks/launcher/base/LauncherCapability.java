package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.api.launcher.IDelayedLauncher;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.blocks.launcher.screen.BlockScreenCause;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.content.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import icbm.classic.lib.transform.rotation.EulerAngle;
import lombok.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class LauncherCapability implements IDelayedLauncher, INBTSerializable<NBTTagCompound> {

    @Getter
    private static final EulerAngle angle = new EulerAngle(0, 0, 0);
    @Getter
    private static final Vec3d SPAWN_OFFSET = new Vec3d(0.5f, 3.1f, 0.5f);
    @Getter @NonNull
    private final TileLauncherBase host;

    /**
     * The internal delay the tile launcher has configured
     * If the value is -1, it will be ignored
     */
    @Getter @Setter
    private int delay = -1;
    /**
     * Tick delay when to fire. If the value is -1 then it's not firing
     */
    @Getter
    private int firingTick = -1;
    private int currentTick = 0;

    @Nullable
    private IMissileTarget targetDataStorage;

    @Override
    public IActionStatus launch(IMissileTarget targetData, @Nullable IMissileCause cause, boolean simulate, int controllerDelay) {
        // Pre-flight checks
        // TODO add a way to bypass in the launcher or cause settings
        var pfc = preflightCheck(targetData);
        if (pfc.isError()) return pfc;

        var time = Math.max(controllerDelay, 0) + Math.max(delay, 0);
        if (time > 0) {
            // TODO prelaunch event?
            firingTick = (20 * time);
            return LauncherStatus.ERROR_LAUNCHING;
        }

        // Setup source and cause
        final BlockPos pos = host.getPos();
        final BlockCause selfCause = new BlockCause(host.world(), pos, host.getBlockState()); // TODO add more information about launcher
        selfCause.setPreviousCause(cause);

        final MissileSource source = new MissileSource(host.world(), SPAWN_OFFSET.addVector(pos.getX(), pos.getY(), pos.getZ()), selfCause);

        //Allow canceling missile launches
        final LauncherEvent.PreLaunch event = new LauncherEvent.PreLaunch(source, this, host.missileHolderCapability, targetData, simulate);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            if(event.cancelReason != null) {
                return event.cancelReason;
            }
            return LauncherStatus.SUCCESS_CANCELED;
        }

        final ItemStack stack = host.missileHolderCapability.getMissileStack();
        if (stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
        {
            final ICapabilityMissileStack missileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
            if (missileStack != null)
            {
                // TODO we may need to walk cause history to get correct launcher count info
                final Vec3d target = applyInaccuracy(targetData.getPosition(), cause instanceof BlockScreenCause ? ((BlockScreenCause) cause).getLauncherCount() : 1);

                //TODO add distance check? --- something seems to be missing

                if (host.isServer() && !simulate)
                {
                    // Should always work but in rare cases capability might have failed
                    if(!host.missileHolderCapability.consumeMissile()) {
                        return LauncherStatus.ERROR_INVALID_STACK;
                    }

                    final IMissile missile = missileStack.newMissile(host.world());
                    final Entity entity = missile.getMissileEntity();

                    // TODO raytrace to make sure we don't teleport through the ground
                    //  raytrace for missile spawn area
                    //  raytrace to check for blockage in silo path... players will be happy about this
                    entity.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);

                    //Trigger launch event
                    missile.setTargetData(new BallisticTargetingData(target, 1));
                    missile.setFlightLogic(new BallisticFlightLogic(host.getLockHeight()));
                    missile.setMissileSource(source); //TODO encode player that built launcher, firing method (laser, remote, redstone), and other useful data
                    missile.launch();

                    //Spawn entity
                    if(!host.world().spawnEntity(entity)) {
                        return LauncherStatus.ERROR_SPAWN;
                    }

                    // consume power
                    host.extractEnergy();

                    //Grab rider
                    if (host.seat != null && !host.seat.getPassengers().isEmpty()) //TODO add hook to disable riding some missiles
                    {
                        final List<Entity> riders = host.seat.getPassengers();
                        riders.forEach(r -> {
                            entity.dismountRidingEntity();
                            r.startRiding(entity);
                        });
                    }
                }
                return LauncherStatus.SUCCESS_LAUNCHED;
            }
        }
        return LauncherStatus.ERROR_INVALID_STACK;
    }

    // TODO pass args same as launch on launchinternal instead?
    private IActionStatus launchInternal() {
        // always run pfc
        var pfc = preflightCheck(targetDataStorage, true);
        if (pfc.isError()) return pfc;

        // TODO tempvars delet me
        IMissileCause cause = null;
        var simulate = false;

        // double check if the items still exist and check if someone wants to abort the launch
        final BlockPos pos = host.getPos();
        final BlockCause selfCause = new BlockCause(host.world(), pos, host.getBlockState()); // TODO add more information about launcher
        selfCause.setPreviousCause(cause);

        final MissileSource source = new MissileSource(host.world(), SPAWN_OFFSET.addVector(pos.getX(), pos.getY(), pos.getZ()), selfCause);

        final LauncherEvent.PreLaunch event = new LauncherEvent.PreLaunch(source, this, host.missileHolderCapability, targetDataStorage, simulate);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            if(event.cancelReason != null) {
                return event.cancelReason;
            }
            return LauncherStatus.SUCCESS_CANCELED;
        }

        final ItemStack stack = host.missileHolderCapability.getMissileStack();
        if (stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
        {
            final ICapabilityMissileStack missileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
            if (missileStack != null)
            {
                // TODO we may need to walk cause history to get correct launcher count info
                final Vec3d target = applyInaccuracy(targetDataStorage.getPosition(), cause instanceof BlockScreenCause ? ((BlockScreenCause) cause).getLauncherCount() : 1);

                //TODO add distance check? --- something seems to be missing

                if (host.isServer() && !simulate)
                {
                    // Should always work but in rare cases capability might have failed
                    if(!host.missileHolderCapability.consumeMissile()) {
                        return LauncherStatus.ERROR_INVALID_STACK;
                    }

                    final IMissile missile = missileStack.newMissile(host.world());
                    final Entity entity = missile.getMissileEntity();

                    // TODO raytrace to make sure we don't teleport through the ground
                    //  raytrace for missile spawn area
                    //  raytrace to check for blockage in silo path... players will be happy about this
                    entity.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);

                    //Trigger launch event
                    missile.setTargetData(new BallisticTargetingData(target, 1));
                    missile.setFlightLogic(new BallisticFlightLogic(host.getLockHeight()));
                    missile.setMissileSource(source); //TODO encode player that built launcher, firing method (laser, remote, redstone), and other useful data
                    missile.launch();

                    //Spawn entity
                    if(!host.world().spawnEntity(entity)) {
                        return LauncherStatus.ERROR_SPAWN;
                    }

                    // consume power
                    host.extractEnergy();

                    //Grab rider
                    if (host.seat != null && !host.seat.getPassengers().isEmpty()) //TODO add hook to disable riding some missiles
                    {
                        final List<Entity> riders = host.seat.getPassengers();
                        riders.forEach(r -> {
                            entity.dismountRidingEntity();
                            r.startRiding(entity);
                        });
                    }
                }
                return LauncherStatus.SUCCESS_LAUNCHED;
            }
        }
        return LauncherStatus.ERROR_INVALID_STACK;
    }

    private IActionStatus preflightCheck(IMissileTarget targetData){
        return preflightCheck(targetData, false);
    }
    private IActionStatus preflightCheck(IMissileTarget targetData, boolean ignoreIsLaunching) {
        if(targetData == null || targetData.getPosition() == null) {
            return LauncherStatus.ERROR_TARGET_NULL;
        }
        else if(isTargetTooClose(targetData.getPosition())) {
            return LauncherStatus.ERROR_MIN_RANGE;
        }
        else if(isTargetTooFar(targetData.getPosition())) {
            return LauncherStatus.ERROR_MAX_RANGE;
        }
        else if(!host.checkExtract()) {
            return LauncherStatus.ERROR_POWER;
        }
        else if(host.missileHolderCapability.getMissileStack().isEmpty()) {
            return LauncherStatus.ERROR_EMPTY_STACK;
        } else if (firingTick != -1 && !ignoreIsLaunching) {
            return LauncherStatus.ERROR_LAUNCHING;
        }
        return LauncherStatus.SUCCESS_GENERIC;
    }

    @Override
    public float getInaccuracy(Vec3d target, int launcherCount) {
        // Apply inaccuracy
        float inaccuracy = (float)ConfigLauncher.MIN_INACCURACY;

        // Add inaccuracy based on range
        final double distance = host.getDistanceSq(target.x, target.y, target.z);
        final double scale = distance / ConfigLauncher.RANGE;
        inaccuracy += scale * ConfigLauncher.SCALED_INACCURACY;

        // Add inaccuracy for each launcher fired in circuit
        if(launcherCount > 1) {
            inaccuracy += (launcherCount - 1) * ConfigLauncher.SCALED_LAUNCHER_COST;
        }
        return inaccuracy;
    }

    @Override
    public int getLauncherGroup() {
        return host.getGroup();
    }

    @Override
    public int getLaunchIndex() {
        return host.getGroupIndex();
    }

    protected Vec3d applyInaccuracy(Vec3d target, int launcherCount)
    {

        //Randomize distance
        float inaccuracy = getInaccuracy(target, launcherCount) * host.world().rand.nextFloat();

        //Randomize radius drop
        angle.setYaw(host.world().rand.nextFloat() * 360); //TODO fix to use a normal distribution from ICBM 2

        //Apply inaccuracy to target position and return
        return new Vec3d(target.x + angle.x() * inaccuracy, 0, target.z + angle.z() * inaccuracy);
    }

    /**
     * Checks to see if the target is too close.
     *
     * @param target
     * @return
     */
    public boolean isTargetTooClose(Vec3d target)
    {
        final int minDistance = 10;
        final double deltaX = Math.abs(target.x - (host.getPos().getX() + 0.5));
        final double  deltaZ = Math.abs(target.z - (host.getPos().getZ() + 0.5));
        return deltaX < minDistance || deltaZ < minDistance;
    }

    public boolean isTargetTooFar(Vec3d target)
    {
        final double deltaX = Math.abs(target.x - (host.getPos().getX() + 0.5));
        final double  deltaZ = Math.abs(target.z - (host.getPos().getZ() + 0.5));
        return deltaX > ConfigLauncher.RANGE || deltaZ > ConfigLauncher.RANGE;
    }

    public void onTick() {
        if (!host.isServer()) {
            return;
        }
        if (firingTick != -1) {
            currentTick++;
            if (firingTick < currentTick) {
                launchInternal();
                currentTick = 0;
                firingTick = -1;
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("delay", delay);
        nbt.setInteger("firingTick", firingTick);
        nbt.setInteger("currentTick", currentTick);
        if (targetDataStorage != null) {
            nbt.setTag("targetData", targetDataStorage.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        delay = Math.max(nbt.getInteger("delay"), -1);
        firingTick = Math.max(nbt.getInteger("firingTick"), -1);
        currentTick = Math.max(nbt.getInteger("currentTick"), 0);
        try {
            // if the data doesn't exist we do not want to create a new TargetData class
            var targetDataNBT = nbt.getCompoundTag("targetData");
            targetDataStorage = new BasicTargetData();
            targetDataStorage.deserializeNBT(targetDataNBT);
        } catch (ReportedException e) {
            // noop
        }
    }
}
