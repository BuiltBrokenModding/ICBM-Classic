package icbm.classic.world.block.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.ITick;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Used to store firing information when working with countdowns/delays
 */
@Data
public class FiringPackage implements INBTSerializable<CompoundTag>, ITick {

    /**
     * Input: Target data
     */
    private IMissileTarget targetData;

    /**
     * Input: Cause of firing the missile
     */
    private IMissileCause cause;

    /**
     * Counter: Time to tick down before firing
     */
    private int countDown = -1;

    private boolean hasFired = false;

    public FiringPackage(IMissileTarget targetData, IMissileCause cause, int countDown) {
        this.targetData = targetData;
        this.cause = cause;
        this.countDown = countDown;
    }

    public FiringPackage(IMissileTarget targetData, IMissileCause cause) {
        this.targetData = targetData;
        this.cause = cause;
    }

    public void launch(IMissileLauncher missileLauncher) {

        if (!hasFired) {
            hasFired = true;

            if (targetData instanceof IMissileTargetDelayed) {
                targetData = ((IMissileTargetDelayed) targetData).cloneWithoutDelay();
            }

            // Invoke normal launch so we fire events and handle logic consistently
            missileLauncher.launch((launcher) -> targetData, cause, false); //TODO add callback to firing source
        }
    }

    public boolean isReady() {
        return !hasFired;
    }

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<FiringPackage> SAVE_LOGIC = new NbtSaveHandler<FiringPackage>()
        .mainRoot()
        /* */.nodeInteger("countdown", FiringPackage::getCountDown, FiringPackage::setCountDown)
        /* */.nodeBuildableObject("target", ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY, FiringPackage::getTargetData, FiringPackage::setTargetData)
        /* */.nodeBuildableObject("cause", ICBMClassicAPI.MISSILE_CAUSE_REGISTRY, FiringPackage::getCause, FiringPackage::setCause)
        .base();

    @Override
    public void update(int tick, boolean isServer) {
        if (isServer && !hasFired) {
            this.countDown--;
            if (countDown <= 0) {
                // TODO launch();
            }
        }
    }
}
