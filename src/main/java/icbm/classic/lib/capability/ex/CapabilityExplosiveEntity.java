package icbm.classic.lib.capability.ex;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveEntity implements IExplosive
{
    public final Entity entity;
    private ItemStack stack = ItemStack.EMPTY;

    public CapabilityExplosiveEntity(@Nonnull Entity entity)
    {
        this.entity = entity;
    }

    public NBTTagCompound serializeNBT()
    {
        return toStack().serializeNBT();
    }

    public void deserializeNBT(@Nonnull NBTTagCompound nbt)
    {
        if (nbt.getSize() == 0)
        {
            stack = ItemStack.EMPTY;
        }
        else
        {
            stack = new ItemStack(nbt);
        }
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        final ItemStack stack = toStack();
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive explosive = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null)
            {
                return explosive.getExplosiveData();
            }
        }
        return ICBMExplosives.CONDENSED;
    }

    @Nonnull
    @Override
    public NBTTagCompound getCustomBlastData()
    {
        final ItemStack stack = toStack();
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive explosive = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null)
            {
                final NBTTagCompound tag = explosive.getCustomBlastData();
                if (tag != null && !tag.hasNoTags())
                {
                    return tag;
                }
            }
        }
        return new NBTTagCompound();
    }

    @Nonnull
    @Override
    public ItemStack toStack()
    {
        if (stack == null)
        {
            stack = ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public void onDefuse()
    {
        entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, toStack().copy()));
        entity.setDead();
    }

    public void setStack(@Nonnull ItemStack stack)
    {
        if (!stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            ICBMClassic.logger().error("CapabilityExplosive[" + entity + "] Was set with a stack that is not an explosive [" + stack + "]");
        }
        this.stack = stack.copy().splitStack(1);
    }

    @Override
    public int hashCode() {
        return 31 * stack.getItem().hashCode() + stack.getItemDamage();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) || other instanceof IExplosive && ItemStack.areItemsEqual(((IExplosive) other).toStack(), toStack());
    }

    @Override
    public String toString() {
        return String.format("CapabilityExplosiveEntity[%s]@%s", toStack(), super.hashCode());
    }
}
