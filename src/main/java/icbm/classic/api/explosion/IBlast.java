package icbm.classic.api.explosion;

import icbm.classic.api.IWorldPosition;

/** The actual explosion interface. Extends Explosion.java.
 *
 * @author Calclavia */
public interface IBlast extends IWorldPosition
{
    /** @return The radius of effect of the explosion. */
    float getRadius();
}
