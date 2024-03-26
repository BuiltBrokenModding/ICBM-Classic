package icbm.classic.api.explosion;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/10/2019.
 *
 * @deprecated entity that spawns the blast will now control this
 */
@Deprecated
public interface IBlastMovable extends IBlast {
    /**
     * Can this blast be moved. Used for
     * redmatter like explosives that can
     * be pushed around and travel.
     *
     * @return
     */
    boolean isMovable();

    /**
     * Called each time the blast is moved by its controller
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    void onPositionUpdate(double posX, double posY, double posZ);
}
