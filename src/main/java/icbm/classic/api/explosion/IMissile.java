package icbm.classic.api.explosion;


import icbm.classic.lib.transform.vector.Pos;

/**
 * This is an interface applied by all missile entities. You may cast this into an @Entity. The
 * "set" version of the function will make the entity do the action on the next tick.
 *
 * @author Calclavia
 */
@Deprecated //Will be recoded
public interface IMissile extends IExplosiveContainer
{
    /**
     * Called to trigger the missile's explosion logic
     */
    void doExplosion();

    boolean isExploding();

    /**
     * Called to ask the missile to blow up on next tick
     *
     * @param fullExplosion -
     *                      True will trigger a the missile's normal explosion
     *                      False will trigger a TNT explosion
     */
    void destroyMissile(boolean fullExplosion);

    /**
     * Called to ask the missile to blow up on next tick
     */
    void triggerExplosion();

    /** Drops the specified missile as an item. */
    void dropMissileAsItem();

    /**
     * The amount of ticks this missile has been flying for. Returns -1 if the missile is not
     * flying.
     */
    int getTicksInAir();

    /** Gets the launcher this missile is launched from. */
    ILauncherContainer getLauncher();

    /**
     * Launches the missile into a specific target.
     *
     * @param target
     */
    void launch(Pos target);

    void launch(Pos target, int height);
}
