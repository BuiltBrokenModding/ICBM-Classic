package icbm.classic.content.blocks.launcher.screen.gui;

import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/27/2018.
 */
public class ContainerLaunchScreen extends ContainerBase<TileLauncherScreen>
{
    public ContainerLaunchScreen(int windowId, Player player, TileLauncherScreen node)
    {
        super(MenuReg.LAUNCHER_SCREEN.get(), windowId, player, node);

        // Battery Slot
        this.addSlot(new SlotItemHandler(node.inventory, 0, 141, 47));
        addPlayerInventory(player, 7, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        return transferStackInSlot(player, slotIndex, 1, 1);
    }
}
// PORTED
