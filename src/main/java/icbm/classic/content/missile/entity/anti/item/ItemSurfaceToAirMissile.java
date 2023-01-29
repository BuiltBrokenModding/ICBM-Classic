package icbm.classic.content.missile.entity.anti.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * Previously called Anti-Ballastic (AB) Missile in older version of the mod. Now Surface to Air Missile (SAM) with similar
 * purpose to attack enemy missiles. However, with expanded logic to target any entity found in the air.
 */
public class ItemSurfaceToAirMissile extends ItemICBMBase
{
    public ItemSurfaceToAirMissile()
    {
        super("surface_to_air_missile");
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilitySAMStack());
        return provider;
    }
}
