package icbm.classic.content.missile.entity.anti.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;

/**
 * Previously called Anti-Ballastic (AB) Missile in older version of the mod. Now Surface to Air Missile (SAM) with similar
 * purpose to attack enemy missiles. However, with expanded logic to target any entity found in the air.
 */
public class ItemSurfaceToAirMissile extends ItemICBMBase implements ICapabilityHolderItem
{
    public ItemSurfaceToAirMissile()
    {
        super("surface_to_air_missile", new Item.Properties().stacksTo(1));
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == ICBMClassicAPI.MISSILE_STACK_CAPABILITY)
        {
            return (T) new CapabilitySAMStack();
        }
        return null;
    }
}
// PORTED
