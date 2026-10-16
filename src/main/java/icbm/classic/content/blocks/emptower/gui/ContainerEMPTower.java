package icbm.classic.content.blocks.emptower.gui;

import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/25/2018.
 */
public class ContainerEMPTower extends ContainerBase<TileEMPTower>
{
    public ContainerEMPTower(int windowId, Player player, TileEMPTower tileEMPTower)
    {
        super(MenuReg.EMP_TOWER.get(), windowId, player, tileEMPTower);
        // Battery Slot
        this.addSlot(new SlotItemHandler(tileEMPTower.inventory, 0, 141, 47));
        addPlayerInventory(player, 7, 84);
    }

    /** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return transferStackInSlot(player, slotIndex, 1, 1);
    }
}
// PORTED
