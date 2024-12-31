package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionData;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO merge down with missile into a class called AmmoItem(IActionData, Item.Properties)
//  fully decoupling entity spawning from ammo concept and allowing creative uses of ammo later (raytraces, teleporting)
public class ItemBombDroplet extends ItemBase {

    private final NonNullSupplier<EntityType<EntityBombDroplet>> type;
    public ItemBombDroplet(NonNullSupplier<EntityType<EntityBombDroplet>> type, Item.Properties properties)
    {
        super(properties);
        this.type = type;
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundNBT nbt) {
        //TODO provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        return new ItemStackCapProvider(stack)
            .with(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, () -> new BombletProjectileStack(type));
    }
}
