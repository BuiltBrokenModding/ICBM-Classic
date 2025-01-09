package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherBaseCapability;
import icbm.classic.content.blocks.launcher.status.FiringWithDelay;
import icbm.classic.content.blocks.launcher.status.LaunchedWithMissile;
import icbm.classic.content.blocks.launcher.status.LauncherStatus;
import icbm.classic.content.missile.logic.flight.DirectFlightLogic;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.CausedByBlock;
import icbm.classic.content.reg.ItemReg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CLauncherCapability extends LauncherBaseCapability {

    private final TileCruiseLauncher host;

    @Override
    public IActionStatus getStatus() {
        // Min power check
        if(!host.energyStorage.consumePower(host.getFiringCost(), true)) {
            return LauncherStatus.ERROR_POWER;
        }
        else if(host.getFiringPackage() != null && !getHost().isAimed()) {
            return LauncherStatus.FIRING_AIMING;
        }
        else if(host.getFiringPackage() != null && host.getFiringPackage().getCountDown() > 0) {
            return new FiringWithDelay(host.getFiringPackage().getCountDown());
        }
        else if(!host.canLaunch()) { //TODO break down into detailed feedback and make consistent with base launcher
            return LauncherStatus.ERROR_GENERIC;
        }

        return LauncherStatus.READY;
    }

    @Override
    public IActionStatus preCheckLaunch(IMissileTarget target, @Nullable IActionCause cause) {
        return getStatus();
    }

    @Override
    public IActionStatus launch(ILauncherSolution solution, @Nullable IActionCause cause, boolean simulate) {

        final IMissileTarget target = solution.getTarget(this);

        // Do pre-checks
        final IActionStatus preCheck = preCheckLaunch(target, cause);
        if(preCheck.isBlocking()) {
            return preCheck;
        }
        else if(simulate) { //TODO handle better by checking if we are already aimed
            return LauncherStatus.FIRING_AIMING;
        }

        // Set target so we can aim
        host.setTarget(target.getPosition()); // TODO store IMissileTarget

        // If not aimed, wait for aim and fire
        if(!host.isAimed()) {
            host.setFiringPackage(new FiringPackage(target, cause, 0));
            return LauncherStatus.FIRING_AIMING; // TODO return aiming status, with callback to check if did fire
        }

        final CausedByBlock selfCause = new CausedByBlock(host.getWorld(), host.getPos(), host.getBlockState());
        selfCause.setPreviousCause(cause);
        final IActionSource missileSource = new ActionSource(DimensionType.getKey(getHost().getWorld().getDimension().getType()), new Vec3d(host.getPos().getX() + 0.5, host.getPos().getY() + TileCruiseLauncher.MISSILE__HOLDER_Y, host.getPos().getZ() + 0.5), selfCause);

        if (host.canLaunch()) //TODO update to mirror launch pad better
        {
            final ItemStack inventoryStack = host.missileHolder.getMissileStack();

            if(inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).isPresent()) {
                final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).orElseThrow(IllegalStateException::new);
                if (host.isServer()) {
                    final IMissile missile = capabilityMissileStack.newMissile(host.getWorld());
                    final Entity entity = missile.getMissileEntity();
                    entity.setPosition(missileSource.getPosition().x, missileSource.getPosition().y, missileSource.getPosition().z);

                    // Should always work but in rare cases capability might have failed
                    if (!host.missileHolder.consumeMissile()) {
                        return LauncherStatus.ERROR_INVALID_STACK;
                    }

                    // Check power again, with firing delay things could change
                    if (!host.energyStorage.consumePower(host.getFiringCost(), true)) {
                        return LauncherStatus.ERROR_POWER;
                    }
                    host.energyStorage.consumePower(host.getFiringCost(), false);

                    //Setup missile
                    missile.setMissileSource(missileSource);
                    missile.setTargetData(target);
                    missile.setFlightLogic(new DirectFlightLogic(ConfigMissile.CRUISE_FUEL));
                    missile.launch();

                    if (!host.getWorld().addEntity(entity)) {
                        return LauncherStatus.ERROR_SPAWN;
                    }
                    return new LaunchedWithMissile().setMissile(missile);
                }
                return LauncherStatus.LAUNCHED;
            }
        }
        return LauncherStatus.ERROR_GENERIC;
    }

    @Override
    public float getPayloadVelocity() {
        // TODO find a way to get this from the missile stack
        return host.getMissileHolder().getMissileStack().getItem() == ItemReg.MISSILE_SURFACE_TO_AIR.get()
            ? ConfigMissile.SAM_MISSILE.FLIGHT_SPEED : ConfigMissile.DIRECT_FLIGHT_SPEED;
    }
}
