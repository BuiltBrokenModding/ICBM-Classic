package icbm.classic.lib;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public abstract class CapabilityPrefab implements ICapabilitySerializable<CompoundTag> {

    public abstract boolean isCapability(@Nonnull Capability<?> capability);

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return isCapability(capability);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (isCapability(capability)) {
            return (T) this;
        }
        return null;
    }


    @Override
    public final CompoundTag serializeNBT() {
        CompoundTag tagCompound = new CompoundTag();
        save(tagCompound);
        return tagCompound;
    }

    @Override
    public final void deserializeNBT(CompoundTag nbt) {
        if (nbt != null && !nbt.isEmpty()) {
            load(nbt);
        }
    }

    protected void save(CompoundTag tag) {

    }

    protected void load(CompoundTag tag) {

    }
}
