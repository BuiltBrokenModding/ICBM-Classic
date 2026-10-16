package icbm.classic.content.items;

import icbm.classic.config.ConfigBattery;
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Simple battery to move energy around between devices
 * <p>
 * PORT: energy is stored in {@link ComponentReg#ENERGY} through NeoForge's {@link ComponentEnergyStorage}
 * instead of a serialized EnergyBuffer capability.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 3/21/2018.
 */
public class ItemBattery extends ItemICBMBase implements ICapabilityHolderItem
{
    public ItemBattery()
    {
        super("battery", new Item.Properties().stacksTo(1));
        //TODO add subtypes (Single Use battery, EMP resistant battery)
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == Capabilities.EnergyStorage.ITEM)
        {
            return (T) new ComponentEnergyStorage(stack, ComponentReg.ENERGY.get(), ConfigBattery.BATTERY_CAPACITY, ConfigBattery.BATTERY_INPUT_LIMIT, ConfigBattery.BATTERY_OUTPUT_LIMIT);
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        final IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null)
        {
            double p = getDurabilityForDisplay(stack) * 100;
            list.add(Component.literal("L: " + (int) p + "%"));
            list.add(Component.literal("E: " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored() + " FE"));
        }
        //TODO add info
        //TODO add shift info (input & output limits)
    }

    /** 1.12 durability helper, the bar itself was never shown */
    public double getDurabilityForDisplay(ItemStack stack)
    {
        final IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.getMaxEnergyStored() > 0)
        {
            return energyStorage.getEnergyStored() / (double) energyStorage.getMaxEnergyStored();
        }
        return 1;
    }
}
// PORTED
