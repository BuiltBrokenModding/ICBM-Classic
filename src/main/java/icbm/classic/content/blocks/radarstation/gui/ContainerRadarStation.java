package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation>
{
    public ContainerRadarStation(@Nullable ContainerType<?> type, int id, PlayerEntity player, TileRadarStation node)
    {
        super(type, id, player, node);

        // Battery Slot
        this.addSlot(new SlotItemHandler(node.getInventory(), 0, 141, 47));

        addPlayerInventory(player, 7, 102);
    }



    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int slotIndex)
    {
        ItemStack targetItemStackCopy = null;
        Slot targetSlot = (net.minecraft.inventory.container.Slot) this.inventorySlots.get(slotIndex);

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
