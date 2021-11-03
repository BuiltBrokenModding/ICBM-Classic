package icbm.classic.api.explosion;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Applied to the object that represents or wrappers the explosion/blast.
 *
 * @author Calclavia, Darkguardsman
 */
public interface IBlast extends IWorldPosition
{
    /**
     * Gets the radius/size of the effect of the blast.
     *
     * This not always the full effect range of the blast.
     * Rather is used more as a scale factor
     *
     * @return size in blocks (meters)
     */
    default float getBlastRadius() //TODO update or add more methods to get true size
    {
        return -1; //TODO move to sub-interface (IScalableBlast) as not all blasts have a radius
    }

    /**
     * Called to start the blast
     *
     * @return this
     */
    @Nonnull
    BlastResponse runBlast();

    /**
     * Is the blast completed and
     * can be marked as dead.
     *
     * @return true for completed
     */
    @Deprecated
    default boolean isCompleted() //TODO merge into BlastState
    {
        return true;
    }

    /**
     * Data used to create the blast. Used
     * for save state recovery
     *
     * @return
     */
    @Nullable
    @Deprecated
    default IExplosiveData getExplosiveData() //TODO move to save handler, this shouldn't be exposed
    {
        return null;
    }

    /**
     * Entity that represents the blast
     * <p>
     * Not all blasts have an entity in the world. Some
     * exist as threaded runners and others as world events.
     * <p>
     * Blasts with entities should be viewed as entities first
     * and blasts second. With the blast existing as an API
     * drive way to provide information and events to interact
     * with the entity's actions.
     *
     * @return controller
     */
    @Nullable
    default Entity getEntity()
    {
        return null;
    }

    /**
     * Gets the entity that the blast originated from during detonation.
     *
     * @return entity, can be null
     */
    @Nullable
    default Entity getBlastSource()
    {
        return getEntity();
    }

    /**
     * Called to clear the blast from the world. This
     * should only be used by server utilities and commands.
     */
    void clearBlast();
}
