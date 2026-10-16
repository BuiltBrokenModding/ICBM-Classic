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
import icbm.classic.lib.RegistryHelper;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.explosive.ExplosiveStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosiveEntity implements IExplosive
{
    public final Entity entity;
    private ItemStack stack = ItemStack.EMPTY;

    private boolean isExploding = false;

    public CapabilityExplosiveEntity(@Nonnull Entity entity)
    {
        this.entity = entity;
    }

    /** PORT: stack saved with {@link ItemStack#saveOptional}, registries from {@link RegistryHelper} */
    public CompoundTag serializeNBT()
    {
        return (CompoundTag) toStack().saveOptional(RegistryHelper.access()); // saveOptional is declared to return Tag
    }

    public void deserializeNBT(@Nonnull CompoundTag nbt)
    {
        if (nbt.isEmpty())
        {
            stack = ItemStack.EMPTY;
        }
        else
        {
            stack = ItemStack.parseOptional(RegistryHelper.access(), nbt);
        }
    }

    public IActionStatus doExplosion(double x, double y, double z, IActionSource source)
    {
        if (this.isExploding) {
            return ActionResponses.COMPLETED;
        }

        //Make sure to note we are currently exploding
        this.isExploding = true;

        if (!this.entity.level().isClientSide())
        {
            return this.getExplosiveData().create(this.entity.level(), x, y, z, source, null).doAction();
        }
        return ActionResponses.COMPLETED;
    }

    @Nonnull
    @Override
    public IExplosiveData getExplosiveData()
    {
        final IExplosive explosive = toStack().getCapability(ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null && explosive.getExplosiveData() != null)
        {
            return explosive.getExplosiveData();
        }
        return ICBMExplosives.CONDENSED;
    }

    @Override
    public void applyCustomizations(IBlast blast) {
        final IExplosive explosive = toStack().getCapability(ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null)
        {
            explosive.applyCustomizations(blast);
        }
    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {
        // PORT: the stack cap writes the customization into the stack, so use the held stack directly
        final IExplosive explosive = toStack().isEmpty() ? null : stack.getCapability(ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null)
        {
            explosive.addCustomization(customization);
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
        entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), toStack().copy()));
        entity.discard();
    }

    public void setStack(@Nonnull ItemStack stack)
    {
        if (stack.getCapability(ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY) == null)
        {
            ICBMClassic.logger().error("CapabilityExplosive[" + entity + "] Was set with a stack that is not an explosive [" + stack + "]");
        }
        this.stack = stack.copyWithCount(1);
    }

    @Override
    public int hashCode() {
        // PORT: 1.12 hashed item + metadata (explosive id)
        return 31 * stack.getItem().hashCode() + Objects.hashCode(ExplosiveStacks.getExplosiveName(stack));
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) || other instanceof IExplosive && sameItemAndExplosive(((IExplosive) other).toStack(), toStack());
    }

    private static boolean sameItemAndExplosive(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        return ItemStack.isSameItem(a, b) && Objects.equals(ExplosiveStacks.getExplosiveName(a), ExplosiveStacks.getExplosiveName(b));
    }

    @Override
    public String toString() {
        return String.format("CapabilityExplosiveEntity[%s]@%s", toStack(), super.hashCode());
    }
}
// PORTED
