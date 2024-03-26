package icbm.classic.prefab.tile;

import net.minecraft.world.entity.player.Player;

/**
 * Created by robert on 1/8/2015.
 */
public interface IGuiTile {
    /**
     * Returns a Server side Container to be displayed to the user.
     *
     * @param ID     The Gui ID Number
     * @param player The player viewing the Gui
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    default Object getServerGuiElement(int ID, Player player) {
        return null;
    }

    /**
     * Returns a Container to be displayed to the user. On the client side, this
     * needs to return a instance of GuiScreen On the server side, this needs to
     * return a instance of Container
     *
     * @param ID     The Gui ID Number
     * @param player The player viewing the Gui
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    default Object getClientGuiElement(int ID, Player player) {
        return null;
    }
}
