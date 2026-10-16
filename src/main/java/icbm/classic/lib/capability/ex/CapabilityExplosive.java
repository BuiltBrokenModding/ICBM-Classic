package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.NBTConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 * <p>
 * PORT: no longer an ICapabilitySerializable/provider, NeoForge capabilities are exposed by the holder
 */
public class CapabilityExplosive implements IExplosive, INBTSerializable<CompoundTag>
{
    public int explosiveID; //TODO change over to resource location or include in save to check for issues using ID only for in memory

    public CapabilityExplosive()
    {
    }

    public CapabilityExplosive(int id)
    {
        this.explosiveID = id;
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return ICBMClassicHelpers.getExplosive(explosiveID, false);
    }

    @Override
    public void applyCustomizations(IBlast blast) {

    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {

    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        return null;
    }

    @Override
    public final CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag tagCompound = new CompoundTag();
        serializeNBT(tagCompound);

        tagCompound.putInt(NBTConstants.EXPLOSIVE_ID, explosiveID);
        return tagCompound;
    }

    protected void serializeNBT(CompoundTag tag)
    {

    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {
        if (nbt.contains(NBTConstants.EXPLOSIVE_ID))
        {
            explosiveID = nbt.getInt(NBTConstants.EXPLOSIVE_ID);
        }
    }

    // PORT: register() (CapabilityManager storage) removed, not needed in NeoForge
}
// PORTED
