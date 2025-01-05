package icbm.classic.lib.capability.emp;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.config.ConfigEMP;
import icbm.classic.lib.InventoryUtility;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper to trigger EMP calls on ItemStack contained inside of {@link ItemEntity}
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpEntityItem implements IEMPReceiver, ICapabilityProvider
{
    public final ItemEntity entityItem;

    private final LazyOptional<CapabilityEmpEntityItem> lazyOptional = LazyOptional.of(() -> this);

    public CapabilityEmpEntityItem(ItemEntity entityItem)
    {
        this.entityItem = entityItem;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (ConfigEMP.ALLOW_GROUND_ITEMS)
        {
            ItemStack stack = entityItem.getItem();
            if (!stack.isEmpty())
            {
                //Copy to prevent changes on real item
                stack = stack.copy();

                //Run call
                power = CapabilityEmpInventory.empItemStack(stack, world, x, y, z, entityItem, emp_blast, power, doAction);

                //Check for delta
                if (doAction && !InventoryUtility.stacksMatchExact(stack, entityItem.getItem()))
                {
                    entityItem.setItem(stack);
                }
            }
        }
        return power;
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        return capability == CapabilityEMP.EMP ? lazyOptional.cast() : LazyOptional.empty();
    }
}
