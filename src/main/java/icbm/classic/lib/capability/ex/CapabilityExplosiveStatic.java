package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.NBTConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveStatic implements IExplosive, INBTSerializable<NBTTagCompound>
{
    private final IExplosiveData data;
    private final Supplier<ItemStack> itemStackSupplier;
    private NBTTagCompound custom_ex_data;

    public CapabilityExplosiveStatic(IExplosiveData data, Supplier<ItemStack> itemStackSupplier) {
        this.data = data;
        this.itemStackSupplier = itemStackSupplier;
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return data;
    }

    @Nonnull
    @Override
    public NBTTagCompound getCustomBlastData()
    {
        if (custom_ex_data == null)
        {
            custom_ex_data = new NBTTagCompound();
        }
        return custom_ex_data;
    }

    public void setCustomData(NBTTagCompound data)
    {
        this.custom_ex_data = data;
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        return itemStackSupplier.get();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        //Do not save the stack itself as we are saving to its NBT
        NBTTagCompound save = new NBTTagCompound(); //TODO do not create empty NBT if we have nothing to save
        if (!getCustomBlastData().hasNoTags())
        {
            save.setTag(NBTConstants.CUSTOM_EX_DATA, getCustomBlastData());
        }
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(NBTConstants.CUSTOM_EX_DATA))
        {
            custom_ex_data = nbt.getCompoundTag(NBTConstants.CUSTOM_EX_DATA);
        }
    }
}
