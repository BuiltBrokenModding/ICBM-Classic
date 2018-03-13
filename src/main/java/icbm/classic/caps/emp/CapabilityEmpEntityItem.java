package icbm.classic.caps.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import icbm.classic.prefab.inventory.InventoryUtility;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Wrapper to trigger EMP calls on ItemStack contained inside of {@link EntityItem}
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpEntityItem implements IEMPReceiver
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
}
