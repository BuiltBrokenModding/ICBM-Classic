package icbm.classic.content.machines.launcher.cruise;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCruiseLauncher extends ContainerBase<TileCruiseLauncher>
{
    public ContainerCruiseLauncher(EntityPlayer player, TileCruiseLauncher tileEntity)
    {
        super(player, tileEntity);
        // Missile Slot
        this.addSlotToContainer(new Slot(tileEntity.getInventory(), 0, 151, 23));
        // Battery Slot
        this.addSlotToContainer(new Slot(tileEntity.getInventory(), 1, 151, 47));
        addPlayerInventory(player);
    }

    /** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot) this.inventorySlots.get(par1);

        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            if (par1 > 1)
            {
                if (this.getSlot(0).isItemValid(var4))
                {
                    if (!this.mergeItemStack(var4, 0, 1, false))
                    {
                        return null;
                    }
                }
                else if (this.getSlot(1).isItemValid(var4))
                {
                    if (!this.mergeItemStack(var4, 1, 2, false))
                    {
                        return null;
                    }
                }
            }
            else if (!this.mergeItemStack(var4, 2, 36 + 2, false))
            {
                return null;
            }

            if (var4.getCount() == 0)
            {
                var3.putStack((ItemStack) null);
            }
            else
            {
                var3.onSlotChanged();
            }

            if (var4.getCount() == var2.getCount())
            {
                return null;
            }

            var3.onTake(par1EntityPlayer, var4);
        }

        return var2;
    }
}
