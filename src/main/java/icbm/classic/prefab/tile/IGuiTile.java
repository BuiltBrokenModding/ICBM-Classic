package icbm.classic.prefab.tile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created by Robin on 1/8/2015.
 * <p>
 * PORT: 1.12 IGuiHandler getServerGuiElement/getClientGuiElement are replaced by {@link MenuProvider}.
 * Tiles implement {@code createMenu(int, Inventory, Player)} and {@code getDisplayName()}; screens are
 * bound to menu types in ClientReg.
 */
public interface IGuiTile extends MenuProvider
{
    /**
     * Opens this tile's menu for the player, server side only. Writes the tile position so
     * {@link icbm.classic.content.reg.MenuReg} can resolve the tile client side.
     *
     * @param player to open the menu for
     */
    default void openGui(Player player)
    {
        if (player instanceof ServerPlayer && this instanceof BlockEntity)
        {
            final BlockEntity tile = (BlockEntity) this;
            player.openMenu(this, buf -> buf.writeBlockPos(tile.getBlockPos()));
        }
    }
}
// PORTED
