package icbm.classic.lib.capability.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import icbm.classic.lib.InventoryUtility;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper to trigger EMP calls on ItemStack contained inside of {@link ItemEntity}
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpItemEntity implements IEMPReceiver, ICapabilityProvider {
    public final ItemEntity entityItem;

    public CapabilityEmpItemEntity(ItemEntity entityItem) {
        this.entityItem = entityItem;
    }

    @Override
    public float applyEmpAction(Level level, double x, double y, double z, IBlast emp_blast, float power, boolean doAction) {
        if (ConfigEMP.ALLOW_GROUND_ITEMS) {
            ItemStack stack = entityItem.getItem();
            if (!stack.isEmpty()) {
                //Copy to prevent changes on real item
                stack = stack.copy();

                //Run call
                power = CapabilityEmpInventory.empItemStack(stack, world, x, y, z, entityItem, emp_blast, power, doAction);

                //Check for delta
                if (doAction && !InventoryUtility.stacksMatchExact(stack, entityItem.getItem())) {
                    entityItem.setItem(stack);
                }
            }
        }
        return power;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityEMP.EMP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return capability == CapabilityEMP.EMP ? (T) this : null;
    }
}
