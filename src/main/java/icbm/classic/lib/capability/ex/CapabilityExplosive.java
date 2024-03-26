package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.NBTConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.item.ItemStack;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;
import net.neoforged.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosive implements IExplosive, ICapabilitySerializable<CompoundTag> {
    public int explosiveID; //TODO change over to resource location or include in save to check for issues using ID only for in memory
    public CompoundTag blastNBT;

    public CapabilityExplosive() {
    }

    public CapabilityExplosive(int id) {
        this.explosiveID = id;
    }

    @Nullable
    @Override
    public ExplosiveType getExplosiveData() {
        return ICBMClassicHelpers.getExplosive(explosiveID, false);
    }

    @Nonnull
    @Override
    public CompoundTag getCustomBlastData() {
        if (blastNBT == null) {
            blastNBT = new CompoundTag();
        }
        return blastNBT;
    }

    public void setCustomData(CompoundTag data) {
        blastNBT = data;
    }

    @Nullable
    @Override
    public ItemStack toStack() {
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return (T) this;
        }
        return null;
    }

    @Override
    public final CompoundTag serializeNBT() {
        final CompoundTag tagCompound = new CompoundTag();
        serializeNBT(tagCompound);

        tagCompound.setInteger(NBTConstants.EXPLOSIVE_ID, explosiveID);
        tagCompound.put(NBTConstants.BLAST_DATA, getCustomBlastData());
        return tagCompound;
    }

    protected void serializeNBT(CompoundTag tag) {

    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.EXPLOSIVE_ID)) {
            explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);
        }
        if (blastNBT == null || nbt.contains(NBTConstants.BLAST_DATA)) {
            blastNBT = nbt.getCompound(NBTConstants.BLAST_DATA);
        }
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IExplosive.class, new Capability.IStorage<IExplosive>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IExplosive> capability, IExplosive instance, Direction side) {
                    if (instance instanceof CapabilityExplosive) {
                        return ((CapabilityExplosive) instance).serializeNBT();
                    }
                    return null;
                }

                @Override
                public void readNBT(Capability<IExplosive> capability, IExplosive instance, Direction side, NBTBase nbt) {
                    if (instance instanceof CapabilityExplosive && nbt instanceof CompoundTag) {
                        ((CapabilityExplosive) instance).deserializeNBT((CompoundTag) nbt);
                    }
                }
            },
            () -> new CapabilityExplosive(-1));
    }
}
