package icbm.classic.api.explosion;

/** Applied to TileEntities that contains missiles within them.
 *
 * @author Calclavia */
@Deprecated //Will be recoded
public interface ILauncherContainer
{
    /** Retrieves the launcher controller controlling this container. */
    ILauncherController getController();
}
