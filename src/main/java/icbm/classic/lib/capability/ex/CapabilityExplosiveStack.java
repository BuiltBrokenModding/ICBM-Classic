package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.NBTConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveStack implements IExplosive, ICapabilitySerializable<CompoundTag> {
    private final ItemStack stack;
    private CompoundTag custom_ex_data;

    public CapabilityExplosiveStack(ItemStack stack) {
        this.stack = stack;
    }

    protected int getExplosiveID() {
        if (stack == null) {
            return 0;
        }
        return stack.getItemDamage(); //TODO replace meta usage for 1.14 update
    }

    @Nullable
    @Override
    public ExplosiveType getExplosiveData() {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(getExplosiveID());
    }

    @Nonnull
    @Override
    public CompoundTag getCustomBlastData() {
        if (custom_ex_data == null) {
            custom_ex_data = new CompoundTag();
        }
        return custom_ex_data;
    }

    public void setCustomData(CompoundTag data) {
        this.custom_ex_data = data;
    }

    @Nullable
    @Override
    public ItemStack toStack() {
        if (stack == null) {
            return new ItemStack(BlockReg.blockExplosive, 1, 0);
        }
        final ItemStack re = stack.copy();
        re.setCount(1);
        return re;
    }

    @Override
    public CompoundTag serializeNBT() {
        //Do not save the stack itself as we are saving to its NBT
        CompoundTag save = new CompoundTag(); //TODO do not create empty NBT if we have nothing to save
        if (!getCustomBlastData().isEmpty()) {
            save.put(NBTConstants.CUSTOM_EX_DATA, getCustomBlastData());
        }
        return save;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.CUSTOM_EX_DATA)) {
            custom_ex_data = nbt.getCompound(NBTConstants.CUSTOM_EX_DATA);
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(this);
        }
        return null;
    }
}
