package icbm.classic.content.blocks.emptower.gui;

import icbm.classic.content.blocks.emptower.EmpTowerInventory;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/25/2018.
 */
public class ContainerEMPTower extends ContainerBase<TileEMPTower>
{
    public ContainerEMPTower(EntityPlayer player, TileEMPTower tileEMPTower)
    {
        super(player, tileEMPTower);
        // Battery Slot
        this.addSlotToContainer(new SlotItemHandler(tileEMPTower.inventory, EmpTowerInventory.SLOT_ENERGY, 141, 47));
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

            if (slotIndex > 0)
            {
                if (this.getSlot(0).isItemValid(targetItemStack))
                {
                    if (!this.mergeItemStack(targetItemStack, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (!this.mergeItemStack(targetItemStack, 1, 36 + 1, false))
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
