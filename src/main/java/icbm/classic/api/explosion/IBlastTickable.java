package icbm.classic.api.explosion;

/**
 * Applied to blasts that exist in world and tick
 * Created by Dark(DarkGuardsman, Robert) on 2/10/2019.
 */
public interface IBlastTickable extends IBlast
{
    /**
     * Called each tick the blast is alive.
     * <p>
     * Normally called from {@link #getController()}
     *
     * @param ticksExisted - ticks the controller is alive
     * @return true to set dead
     */
    boolean onBlastTick(int ticksExisted);
}
