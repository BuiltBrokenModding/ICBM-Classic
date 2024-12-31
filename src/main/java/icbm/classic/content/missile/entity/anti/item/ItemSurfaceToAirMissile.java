package icbm.classic.content.missile.entity.anti.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

/**
 * Previously called Anti-Ballastic (AB) Missile in older version of the mod. Now Surface to Air Missile (SAM) with similar
 * purpose to attack enemy missiles. However, with expanded logic to target any entity found in the air.
 */
public class ItemSurfaceToAirMissile extends ItemBase
{
    public ItemSurfaceToAirMissile(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.with("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilitySAMStack());
        return provider;
    }
}
