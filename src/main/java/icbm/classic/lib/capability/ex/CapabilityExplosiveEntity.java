package icbm.classic.lib.capability.ex;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.actions.status.ActionResponses;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosiveEntity implements IExplosive
{
    public final Entity entity;

    private boolean isExploding = false;

    public CapabilityExplosiveEntity(@Nonnull Entity entity)
    {
        this.entity = entity;
    }

    public CompoundNBT serializeNBT()
    {
        return toStack().serializeNBT();
    }

    public void deserializeNBT(@Nonnull CompoundNBT nbt)
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

    public IActionStatus doExplosion(double x, double y, double z, IActionSource source)
    {
        if (this.isExploding) {
            return ActionResponses.COMPLETED;
        }

        //Make sure to note we are currently exploding
        this.isExploding = true;

        if (!this.entity.world.isRemote)
        {
            return this.getExplosiveData().create(this.entity.world, x, y, z, source, null).doAction();
        }
        return ActionResponses.COMPLETED;
    }

    @Nonnull
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

    @Override
    public void applyCustomizations(IBlast blast) {
        final ItemStack stack = toStack();
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive explosive = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null)
            {
                explosive.applyCustomizations(blast);
            }
        }
    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {
        final ItemStack stack = toStack();
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive explosive = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null)
            {
                explosive.addCustomization(customization);
            }
        }
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
        entity.world.spawnEntity(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, toStack().copy()));
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
