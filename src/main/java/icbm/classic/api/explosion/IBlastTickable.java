package icbm.classic.api.explosion;

/**
 * Applied to blasts that exist in world and tick
 * Created by Dark(DarkGuardsman, Robin) on 2/10/2019.
 *
 * @deprecated being replaced with {@link icbm.classic.api.actions.IAction} which will not
 * support tickable actions. Any action that threads or has real time updates will be reworked
 * to use another system or spawn an entity to act as a listener.
 */
public interface IBlastTickable extends IBlast
{
    /**
     * Called each tick the blast is alive.
     * <p>
     * Normally called from {@link #getEntity()}
     *
     * @param ticksExisted - ticks the controller is alive
     * @return true to set dead
     */
    boolean onBlastTick(int ticksExisted);

    /**
     * Should the tickable blast be controlled
     * by an explosive entity
     *
     * @return
     */
    default boolean spawnEntity()
    {
        //TODO consider making a method to create the ticking entity (or system) so we can have a different version per explosive
        return true;
    }
}
// PORTED
