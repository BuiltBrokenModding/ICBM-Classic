package icbm.classic.world.block.radarstation.gui;

import icbm.classic.prefab.gui.ContainerBase;
import icbm.classic.world.block.radarstation.TileRadarStation;
import net.minecraft.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.items.SlotItemHandler;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation> {
    public ContainerRadarStation(Player player, TileRadarStation node) {
        super(player, node);

        // Battery Slot
        this.addSlotToContainer(new SlotItemHandler(node.getInventory(), 0, 141, 47));

        addPlayerInventory(player, 7, 102);
    }


    @Override
    public ItemStack transferStackInSlot(Player par1Player, int slotIndex) {
        ItemStack targetItemStackCopy = null;
        Slot targetSlot = (Slot) this.inventorySlots.get(slotIndex);

        if (targetSlot != null && targetSlot.getHasStack()) {
            ItemStack targetItemStack = targetSlot.getStack();
            targetItemStackCopy = targetItemStack.copy();

            if (slotIndex > 0) {
                if (this.getSlot(0).isItemValid(targetItemStack)) {
                    if (!this.mergeItemStack(targetItemStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.mergeItemStack(targetItemStack, 1, 36 + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (targetItemStack.getCount() == 0) {
                targetSlot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            } else {
                targetSlot.onSlotChanged();
            }

            if (targetItemStack.getCount() == targetItemStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            targetSlot.onTake(par1Player, targetItemStack);
        }

        if (targetItemStackCopy == null) {
            return ItemStack.EMPTY;
        }
        return targetItemStackCopy;
    }
}
