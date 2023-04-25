package icbm.classic.content.blocks.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Used to store firing information when working with countdowns/delays
 */
@Data
@AllArgsConstructor
public class FiringPackage implements INBTSerializable<NBTTagCompound> {

    /** Input: Target data */
    private IMissileTarget targetData;

    /** Input: Cause of firing the missile */
    private IMissileCause cause;

    /** Counter: Time to tick down before firing */
    private int countDown;

    public void launch(IMissileLauncher missileLauncher) {
        if(targetData instanceof IMissileTargetDelayed) {
            targetData = ((IMissileTargetDelayed) targetData).cloneWithoutDelay();
        }

        // Invoke normal launch so we fire events and handle logic consistently
        missileLauncher.launch(targetData, cause, false); //TODO add callback to firing source
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<FiringPackage> SAVE_LOGIC = new NbtSaveHandler<FiringPackage>()
        .mainRoot()
        /* */.nodeInteger("countdown", FiringPackage::getCountDown, FiringPackage::setCountDown)
        /* */.nodeBuildableObject("target", ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY, FiringPackage::getTargetData, FiringPackage::setTargetData)
        /* */.nodeBuildableObject("cause", ICBMClassicAPI.MISSILE_CAUSE_REGISTRY, FiringPackage::getCause, FiringPackage::setCause)
        .base();
}
