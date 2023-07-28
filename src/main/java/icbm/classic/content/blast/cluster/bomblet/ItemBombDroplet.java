package icbm.classic.content.blast.cluster.bomblet;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStatic;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBombDroplet extends ItemBase {
    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStatic(ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(0), stack::copy));
        provider.add("projectile", ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, new BombletProjectileStack(stack::copy));
        return provider;
    }
}
