package icbm.classic.content.items;

import icbm.classic.config.ConfigBattery;
import icbm.classic.lib.energy.storage.EnergyBufferLimited;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;

/**
 * Simple battery to move energy around between devices
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class ItemBattery extends ItemICBMBase
{
    public ItemBattery()
    {
        super("battery");
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("battery", CapabilityEnergy.ENERGY, new EnergyBufferLimited(ConfigBattery.BATTERY_CAPACITY, ConfigBattery.BATTERY_INPUT_LIMIT, ConfigBattery.BATTERY_OUTPUT_LIMIT));
        return provider;
    }
}
