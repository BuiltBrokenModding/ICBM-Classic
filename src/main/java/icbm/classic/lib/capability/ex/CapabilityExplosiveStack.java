package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveStack implements IExplosive, ICapabilitySerializable<NBTTagCompound>
{
    public static final String NBT_STACK = "explosive_stack";
    private final ItemStack stack;
    private NBTTagCompound custom_ex_data;

    public CapabilityExplosiveStack(ItemStack stack)
    {
        this.stack = stack;
    }

    protected int getExplosiveID()
    {
        if(stack == null)
        {
            return 0;
        }
        return stack.getItemDamage(); //TODO replace meta usage for 1.14 update
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(getExplosiveID());
    }

    @Nullable
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
        if (stack == null)
        {
            return new ItemStack(BlockReg.blockExplosive, 1, 0);
        }
        final ItemStack re = stack.copy();
        re.setCount(1);
        return re;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        //Do not save the stack itself as we are saving to its NBT
        NBTTagCompound save = new NBTTagCompound();
        if (!getCustomBlastData().isEmpty())
        {
            save.setTag("custom_ex_data", getCustomBlastData());
        }
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey("custom_ex_data"))
        {
            custom_ex_data = nbt.getCompoundTag("custom_ex_data");
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY)
        {
            return (T) this;
        }
        return null;
    }
}
