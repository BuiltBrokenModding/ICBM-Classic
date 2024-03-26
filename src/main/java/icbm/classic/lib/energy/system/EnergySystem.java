package icbm.classic.lib.energy.system;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles wrapping power system support for internal checks and calls
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
public final class EnergySystem {
    private final static IEnergySystem NULL_SYSTEM = new EnergySystemNull();
    private final static List<IEnergySystem> energySystems = new ArrayList();

    public static IEnergySystem getSystem(BlockEntity blockEntity, Direction side) {
        return getSystemForObject(tile, side);
    }

    public static IEnergySystem getSystem(Entity entity, Direction side) {
        return getSystemForObject(entity, side);
    }

    public static IEnergySystem getSystem(ItemStack item, Direction side) {
        return getSystemForObject(item, side);
    }

    public static boolean isEnergyItem(ItemStack stack) {
        return isEnergyItem(stack, null);
    }

    public static boolean isEnergyItem(ItemStack stack, Direction side) {
        return energySystems.stream().anyMatch(system -> system.canSupport(stack, side));
    }

    private static IEnergySystem getSystemForObject(Object object, Direction side) {
        for (IEnergySystem system : energySystems) {
            if (system.canSupport(object, side)) {
                return system;
            }
        }
        return NULL_SYSTEM;
    }

    public static void register(IEnergySystem energySystem) {
        if (energySystem != null) {
            energySystems.add(energySystem);
        }
    }
}
