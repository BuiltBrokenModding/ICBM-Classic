package icbm.classic.content.blocks.battery;

import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.prefab.gui.ContainerBase;
import icbm.classic.prefab.gui.slot.SlotEnergyItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2018.
 */
public class ContainerBattery extends ContainerBase<TileEntityBattery>
{
    public ContainerBattery(EntityPlayer player, TileEntityBattery node)
    {
        super(player, node);
        int x = 44;
        int y = 30;
        for (int slot = 0; slot < TileEntityBattery.SLOTS; slot++)
        {
            this.addSlotToContainer(new SlotEnergyItem(node.getInventory(), slot, slot * 18 + x, y));
        }
        addPlayerInventory(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        final int playerInvStart = TileEntityBattery.SLOTS;
        final int playerInvEnd = playerInvStart + 27;
        final int playerInvHotbarEnd = playerInvEnd + 9;

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            //Merge into battery slots
            if (index >= TileEntityBattery.SLOTS && EnergySystem.getSystem(itemstack, null).canSupport(itemstack, null))
            {
                if (!this.mergeItemStack(itemstack1, 0, TileEntityBattery.SLOTS + 1, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < TileEntityBattery.SLOTS + 1)
            {
                if (!this.mergeItemStack(itemstack1, playerInvStart, playerInvHotbarEnd, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            //From player inventory to hotbar
            else if (index >= playerInvStart && index < playerInvEnd)
            {
                if (!this.mergeItemStack(itemstack1, playerInvEnd, playerInvHotbarEnd, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            //From hotbar to player inventory
            else if (index >= playerInvEnd && index < playerInvHotbarEnd && !this.mergeItemStack(itemstack1, playerInvStart, playerInvEnd, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}
