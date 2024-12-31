package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nullable;

public class ItemMissile extends ItemBase
{
    private final NonNullSupplier<EntityType<EntityExplosiveMissile>> entityType;

    public ItemMissile(NonNullSupplier<EntityType<EntityExplosiveMissile>> entityType, Properties p_i48487_1_) {
        super(p_i48487_1_);
        this.entityType = entityType;
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        //TODO provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        return new ItemStackCapProvider(stack)
            .with(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, () -> new CapabilityMissileStack(entityType));
    }
}
