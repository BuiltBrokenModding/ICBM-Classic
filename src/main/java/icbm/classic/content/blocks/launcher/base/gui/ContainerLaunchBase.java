package icbm.classic.content.blocks.launcher.base.gui;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/27/2018.
 */
public class ContainerLaunchBase extends ContainerBase<TileLauncherBase>
{
    public ContainerLaunchBase(int windowId, Player player, TileLauncherBase tileEntity)
    {
        super(MenuReg.LAUNCHER_BASE.get(), windowId, player, tileEntity);

        // Missile Slot
        this.addSlot(new SlotItemHandler(tileEntity.inventory, 0, 88, 47));
        // Battery Slot
        this.addSlot(new SlotItemHandler(tileEntity.inventory, 1, 141, 47));

        addPlayerInventory(player, 7, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        // PORT: 1.12 treated any slot > 0 as the player side, so the battery slot could never be shift-clicked out
        return transferStackInSlot(player, slotIndex, 2, 2);
    }
}
// PORTED
