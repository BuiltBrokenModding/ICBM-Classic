package icbm.classic.api.explosion;

import icbm.classic.api.IWorldPosition;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

/**
 * Applied to the object that represents or wrappers the explosion/blast.
 *
 * @author Calclavia, Darkguardsman
 */
public interface IBlast extends IWorldPosition
{

    /**
     * Gets the radius size of the effect of the blast.
     * This not always the full effect range of the blast.
     * Rather is used more as a scale factor
     *
     * @return size in blocks (meters)
     */
    float getBlastRadius(); //TODO update or add more methods to get true size

    /**
     * Gets the entity that the blast originated from during detonation.
     *
     * @return entity, can be null
     */
    Entity getBlastSource();

    /**
     * Called to scale the blast by the given amount.
     * <p>
     * Not all blasts can be scaled
     *
     * @param scale
     * @return this
     */
    default IBlast scaleBlast(double scale)
    {
        return this;
    }

    /**
     * Called to start the blast
     *
     * @return this
     */
    BlastState runBlast();

    /**
     * Is the blast completed and
     * can be marked as dead.
     *
     * @return true for completed
     */
    boolean isCompleted();

    /**
     * Data used to create the blast. Used
     * for save state recovery
     *
     * @return
     */
    @Nullable
    IExplosiveData getExplosiveData();

    /**
     * Entity controlling the blast.
     * <p>
     * Not all blasts have an entity that
     * handles updates. Some are controlled
     * by the thread system and others are
     * fired as simple TNT like explosives
     *
     * @return controller
     */
    @Nullable
    Entity getController();

    /**
     * Called to clear the blast from the world. This
     * should only be used by server utilities and commands.
     */
    void clearBlast();

    //TODO expose blast properties
    //TODO expose blast state (init, blocks, entity, done)
    //TODO expose threaded state if used
    //TODO expose tick settings and tick progress
}
