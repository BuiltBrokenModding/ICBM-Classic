package icbm.classic.content.blocks.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.ITick;
import lombok.Data;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Consumer;

/**
 * Used to store firing information when working with countdowns/delays
 */
@Data
public class FiringPackage implements INBTSerializable<CompoundNBT>, ITick {

    /** Input: Target data */
    private IMissileTarget targetData;

    /** Input: Cause of firing the missile */
    private IActionCause cause;

    /** Counter: Time to tick down before firing */
    private int countDown = -1;

    private boolean hasFired = false;

    private Consumer<IActionStatus> onTriggerCallback;

    public FiringPackage(IMissileTarget targetData, IActionCause cause, int countDown) {
        this.targetData = targetData;
        this.cause = cause;
        this.countDown = countDown;
    }

    public FiringPackage(IMissileTarget targetData, IActionCause cause) {
        this.targetData = targetData;
        this.cause = cause;
    }

    public void launch(IMissileLauncher missileLauncher) {

        if(!hasFired) {
            hasFired = true;

            if (targetData instanceof IMissileTargetDelayed) {
                targetData = ((IMissileTargetDelayed) targetData).cloneWithoutDelay();
            }

            // Invoke normal launch so we fire events and handle logic consistently
            final IActionStatus status = missileLauncher.launch((launcher) -> targetData, cause, false);
            if(onTriggerCallback != null) {
                onTriggerCallback.accept(status);
            }
        }
    }

    public boolean isReady() {
        return !hasFired;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this, new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<FiringPackage> SAVE_LOGIC = new NbtSaveHandler<FiringPackage>()
        .mainRoot()
        /* */.nodeInteger("countdown", FiringPackage::getCountDown, FiringPackage::setCountDown)
        /* */.nodeBuildableObject("target", () -> ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY, FiringPackage::getTargetData, FiringPackage::setTargetData)
        /* */.nodeBuildableObject("cause", () -> ICBMClassicAPI.ACTION_CAUSE_REGISTRY, FiringPackage::getCause, FiringPackage::setCause)
        .base();

    @Override
    public void update(int tick, boolean isServer) {
        if(isServer && !hasFired) {
            this.countDown--;
            if(countDown <= 0) {
                // TODO launch();
            }
        }
    }
}
