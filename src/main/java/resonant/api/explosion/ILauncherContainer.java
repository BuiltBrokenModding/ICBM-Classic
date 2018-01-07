package resonant.api.explosion;

import net.minecraft.inventory.IInventory;

/** Applied to TileEntities that contains missiles within them.
 *
 * @author Calclavia */
public interface ILauncherContainer extends IInventory
{
    /** Retrieves the launcher controller controlling this container. */
    public ILauncherController getController();
}
