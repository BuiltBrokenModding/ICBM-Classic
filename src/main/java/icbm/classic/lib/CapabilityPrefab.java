package icbm.classic.lib;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/9/19.
 *
 * PORT: 1.12 version was an ICapabilitySerializable provider. NeoForge capabilities are looked up through
 * {@link icbm.classic.lib.capability.holder} interfaces, so only the save/load template remains. The
 * former isCapability/hasCapability/getCapability methods are gone; expose the object from the holder's
 * getCapability instead.
 */
public abstract class CapabilityPrefab implements INBTSerializable<CompoundTag>
{
    @Override
    public final CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider)
    {
        CompoundTag tagCompound = new CompoundTag();
        save(tagCompound);
        return tagCompound;
    }

    @Override
    public final void deserializeNBT(@Nonnull HolderLookup.Provider provider, CompoundTag nbt)
    {
        if (nbt != null && !nbt.isEmpty())
        {
            load(nbt);
        }
    }

    protected void save(CompoundTag tag)
    {

    }

    protected void load(CompoundTag tag)
    {

    }
}
// PORTED
