package icbm.classic.content.blocks.launcher.base.gui;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/27/2018.
 */
public class ContainerLaunchBase extends ContainerBase<TileLauncherBase>
{
    public ContainerLaunchBase(EntityPlayer player, TileLauncherBase tileEntity)
    {
        super(player, tileEntity);

        // Missile Slot
        this.addSlotToContainer(new SlotItemHandler(tileEntity.inventory, 0, 88, 47));
        // Battery Slot
        this.addSlotToContainer(new SlotItemHandler(tileEntity.inventory, 1, 141, 47));

        addPlayerInventory(player, 7, 84);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex)
    {
        ItemStack targetItemStackCopy = null;
        Slot targetSlot = (Slot) this.inventorySlots.get(slotIndex);

        if (targetSlot != null && targetSlot.getHasStack())
        {
            ItemStack targetItemStack = targetSlot.getStack();
            targetItemStackCopy = targetItemStack.copy();

            if (slotIndex > 0)
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
