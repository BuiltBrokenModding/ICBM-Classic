package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCruiseLauncher extends ContainerBase<TileCruiseLauncher>
{
    public ContainerCruiseLauncher(EntityPlayer player, TileCruiseLauncher tileEntity)
    {
        super(player, tileEntity);
        // Missile Slot
        this.addSlotToContainer(new SlotItemHandler(tileEntity.inventory, 0, 88, 47));
        // Battery Slot
        this.addSlotToContainer(new SlotItemHandler(tileEntity.inventory, 1, 141, 47));
        addPlayerInventory(player, 7, 84);
    }

    /** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex)
    {
        ItemStack targetItemStackCopy = null;
        Slot targetSlot = (Slot) this.inventorySlots.get(slotIndex);

        if (targetSlot != null && targetSlot.getHasStack())
        {
            ItemStack targetItemStack = targetSlot.getStack();
            targetItemStackCopy = targetItemStack.copy();

            if (slotIndex > 1)
            {
                if (this.getSlot(0).isItemValid(targetItemStack))
                {
                    if (!this.mergeItemStack(targetItemStack, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (this.getSlot(1).isItemValid(targetItemStack))
                {
                    if (!this.mergeItemStack(targetItemStack, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (!this.mergeItemStack(targetItemStack, 2, 36 + 2, false))
            {
                return ItemStack.EMPTY;
            }

            if (targetItemStack.getCount() == 0)
            {
                targetSlot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
            else
            {
                targetSlot.onSlotChanged();
            }

            if (targetItemStack.getCount() == targetItemStackCopy.getCount())
            {
                return ItemStack.EMPTY;
            }

            targetSlot.onTake(par1EntityPlayer, targetItemStack);
        }

        if(targetItemStackCopy==null)
        {
            return ItemStack.EMPTY;
        }
        return targetItemStackCopy;
    }
}
