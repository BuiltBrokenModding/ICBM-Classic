package icbm.classic.prefab.tile;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by robert on 1/8/2015.
 */
public interface IGuiTile
{
    /**
     * Returns a Server side Container to be displayed to the user.
     *
     * @param ID     The Gui ID Number
     * @param player The player viewing the Gui
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    default Object getServerGuiElement(int ID, EntityPlayer player)
    {
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
    default Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }

    boolean openGui(EntityPlayer player, int requestedID);


    /**
     * Called to see if right click should be handled
     * automatically and the GUI should be open.
     *
     * @param player - who is trying to open the GUI, in rare cases can be null
     * @return true if should open
     */
    default boolean shouldOpenOnRightClick(EntityPlayer player)
    {
        return true;
    }

    /**
     * Gets the default ID that should be opened
     *
     * @param player - who is trying to open the GUI, in rare cases can be null
     * @return ID of the GUI
     */
    default int getDefaultGuiID(EntityPlayer player)
    {
        return 0;
    }
}
