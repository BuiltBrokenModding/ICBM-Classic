package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation>
{
    public ContainerRadarStation(int windowId, Player player, TileRadarStation node)
    {
        super(MenuReg.RADAR_STATION.get(), windowId, player, node);

        // Battery Slot
        this.addSlot(new SlotItemHandler(node.getInventory(), 0, 141, 47));

        addPlayerInventory(player, 7, 102);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return transferStackInSlot(player, slotIndex, 1, 1);
    }
}
// PORTED
