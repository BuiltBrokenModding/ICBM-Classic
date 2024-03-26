package icbm.classic.world.missile.entity.anti.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

/**
 * Previously called Anti-Ballastic (AB) Missile in older version of the mod. Now Surface to Air Missile (SAM) with similar
 * purpose to attack enemy missiles. However, with expanded logic to target any entity found in the air.
 */
public class ItemSurfaceToAirMissile extends ItemICBMBase {
    public ItemSurfaceToAirMissile(Properties properties) {
        super(properties, "surface_to_air_missile");
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilitySAMStack());
        return provider;
    }
}
