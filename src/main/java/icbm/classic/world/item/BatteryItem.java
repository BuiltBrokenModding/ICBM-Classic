package icbm.classic.world.item;

import icbm.classic.config.ConfigBattery;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Simple battery to move energy around between devices
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class BatteryItem extends ItemICBMBase {

    public BatteryItem(Properties properties) {
        super(properties, "battery");
        setHasSubtypes(true);
        setMaxStackSize(1);
        //TODO add subtypes (Single Use battery, EMP resistant battery)
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("battery", CapabilityEnergy.ENERGY, new EnergyBuffer(() -> ConfigBattery.BATTERY_CAPACITY).withReceiveLimit(() -> ConfigBattery.BATTERY_INPUT_LIMIT).withExtractLimit(() -> ConfigBattery.BATTERY_OUTPUT_LIMIT));
        return provider;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, Level level, List<String> list, ITooltipFlag flag) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage != null) {
                double p = getDurabilityForDisplay(stack) * 100;
                list.add("L: " + (int) p + "%");
                list.add("E: " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored() + " FE");
            }
        }
        //TODO add info
        //TODO add shift info (input & output limits)
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage != null) {
                return energyStorage.getEnergyStored() / (double) energyStorage.getMaxEnergyStored();
            }
        }
        return 1;
    }
}
