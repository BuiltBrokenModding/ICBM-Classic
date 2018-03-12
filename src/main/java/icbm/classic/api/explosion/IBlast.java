package icbm.classic.api.explosion;

import icbm.classic.api.IWorldPosition;
import net.minecraft.entity.Entity;

/**
 * The actual explosion interface. Extends Explosion.java.
 *
 * @author Calclavia
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
}
