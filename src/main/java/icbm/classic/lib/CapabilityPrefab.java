package icbm.classic.lib;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public abstract class CapabilityPrefab implements ICapabilitySerializable<NBTTagCompound>
{

    public abstract boolean isCapability(@Nonnull Capability<?> capability);

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return isCapability(capability);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (isCapability(capability))
        {
            return (T) this;
        }
        return null;
    }


    @Override
    public final NBTTagCompound serializeNBT()
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        save(tagCompound);
        return tagCompound;
    }

    @Override
    public final void deserializeNBT(NBTTagCompound nbt)
    {
        if (nbt != null && !nbt.isEmpty())
        {
            load(nbt);
        }
    }

    protected void save(NBTTagCompound tag)
    {

    }

    protected void load(NBTTagCompound tag)
    {

    }
}
