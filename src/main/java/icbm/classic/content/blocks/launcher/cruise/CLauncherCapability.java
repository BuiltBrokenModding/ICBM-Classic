package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherBaseCapability;
import icbm.classic.content.missile.logic.flight.DirectFlightLogic;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.lib.capability.launcher.data.FiringWithDelay;
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

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
    public IActionStatus preCheckLaunch(IMissileTarget target, @Nullable IMissileCause cause) {
        return getStatus();
    }

    @Override
    public IActionStatus launch(ILauncherSolution solution, @Nullable IMissileCause cause, boolean simulate) {

        final IMissileTarget target = solution.getTarget(this);

        // Do pre-checks
        final IActionStatus preCheck = preCheckLaunch(target, cause);
        if(preCheck.shouldBlockInteraction()) {
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

        final BlockCause selfCause = new BlockCause(host.getWorld(), host.getPos(), host.getBlockState());
        selfCause.setPreviousCause(cause);
        final IMissileSource missileSource = new MissileSource(getHost().getWorld(), new Vec3d(host.getPos().getX() + 0.5, host.getPos().getY() + TileCruiseLauncher.MISSILE__HOLDER_Y, host.getPos().getZ() + 0.5), selfCause);

        if (host.canLaunch()) //TODO update to mirror launch pad better
        {
            final ItemStack inventoryStack = host.missileHolder.getMissileStack();

            if(inventoryStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
                final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
                if(capabilityMissileStack != null) {

                    if(host.isServer()) {
                        final IMissile missile = capabilityMissileStack.newMissile(host.getWorld());
                        final Entity entity = missile.getMissileEntity();
                        entity.setPosition(missileSource.getPosition().x, missileSource.getPosition().y, missileSource.getPosition().z);

                        // Should always work but in rare cases capability might have failed
                        if (!host.missileHolder.consumeMissile()) {
                            return LauncherStatus.ERROR_INVALID_STACK;
                        }

                        // Check power again, with firing delay things could change
                        if(!host.energyStorage.consumePower(host.getFiringCost(), true)) {
                            return LauncherStatus.ERROR_POWER;
                        }
                        host.energyStorage.consumePower(host.getFiringCost(), false);

                        //Setup missile
                        missile.setMissileSource(missileSource);
                        missile.setTargetData(target);
                        missile.setFlightLogic(new DirectFlightLogic(ConfigMissile.CRUISE_FUEL));
                        missile.launch();

                        if (!host.getWorld().spawnEntity(entity)) {
                            return LauncherStatus.ERROR_SPAWN;
                        }
                    }
                    return LauncherStatus.LAUNCHED;
                }
            }
        }
        return LauncherStatus.ERROR_GENERIC;
    }
}
