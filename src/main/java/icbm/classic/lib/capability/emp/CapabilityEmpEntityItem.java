package icbm.classic.lib.capability.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import icbm.classic.prefab.inventory.InventoryUtility;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper to trigger EMP calls on ItemStack contained inside of {@link EntityItem}
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpEntityItem implements IEMPReceiver, ICapabilityProvider
{
    public final EntityItem entityItem;

    public CapabilityEmpEntityItem(EntityItem entityItem)
    {
        this.entityItem = entityItem;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
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

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP ? (T) this : null;
    }
}
