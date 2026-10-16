package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ContainerCruiseLauncher extends ContainerBase<TileCruiseLauncher>
{
    public ContainerCruiseLauncher(int windowId, Player player, TileCruiseLauncher tileEntity)
    {
        super(MenuReg.CRUISE_LAUNCHER.get(), windowId, player, tileEntity);
        // Missile Slot
        this.addSlot(new SlotItemHandler(tileEntity.inventory, 0, 88, 47));
        // Battery Slot
        this.addSlot(new SlotItemHandler(tileEntity.inventory, 1, 141, 47));
        addPlayerInventory(player, 7, 84);
    }

    /** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return transferStackInSlot(player, slotIndex, 2, 2);
    }
}
// PORTED
