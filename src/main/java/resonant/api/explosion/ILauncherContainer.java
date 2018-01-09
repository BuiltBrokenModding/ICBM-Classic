package resonant.api.explosion;

/** Applied to TileEntities that contains missiles within them.
 *
 * @author Calclavia */
public interface ILauncherContainer
{
    /** Retrieves the launcher controller controlling this container. */
    ILauncherController getController();
}
