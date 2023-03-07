package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.api.launcher.IMissileLauncher;
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
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

@Data
@AllArgsConstructor
public class LauncherCapability implements IMissileLauncher {

    private static final Vec3d SPAWN_OFFSET = new Vec3d(0.5f, 3.1f, 0.5f);
    private final TileLauncherBase host;


    @Override
    public IActionStatus launch(IMissileTarget targetData, @Nullable IMissileCause cause, boolean simulate) {

        // Pre-flight checks TODO add a way to bypass in the launcher or cause settings
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
        else if(host.missileHolder.getMissileStack().isEmpty()) {
            return LauncherStatus.ERROR_EMPTY_STACK;
        }

        // Setup source and cause
        final BlockCause selfCause = new BlockCause(host.world(), host.getPos(), host.getBlockState()); // TODO add more information about launcher
        selfCause.setPreviousCause(cause);

        final MissileSource source = new MissileSource(host.world(), SPAWN_OFFSET.addVector(host.getPos().getX(), host.getPos().getY(), host.getPos().getZ()), selfCause);

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
                final Vec3d target = host.applyInaccuracy(targetData.getPosition(), cause instanceof BlockScreenCause ? ((BlockScreenCause) cause).getLauncherCount() : 1);

                //TODO add distance check? --- something seems to be missing

                if (host.isServer() && !simulate)
                {
                    // Should always work but in rare cases capability might have failed
                    if(!host.missileHolder.consumeMissile()) {
                        return LauncherStatus.ERROR_INVALID_STACK;
                    }

                    final IMissile missile = missileStack.newMissile(host.world());
                    final Entity entity = missile.getMissileEntity();

                    // TODO raytrace to make sure we don't teleport through the ground
                    // raytrace for missile spawn area
                    // raytrace to check for blockage in silo path... players will be happy about this
                    entity.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);

                    //Trigger launch event
                    missile.setTargetData(new BallisticTargetingData(target, 1));
                    missile.setFlightLogic(new BallisticFlightLogic(cause instanceof BlockScreenCause ? ((BlockScreenCause) cause).getLockHeight() : 10));
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
                return LauncherStatus.LAUNCHED;
            }
        }
        return LauncherStatus.ERROR_INVALID_STACK;
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
}