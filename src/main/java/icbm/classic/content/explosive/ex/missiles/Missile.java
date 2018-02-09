package icbm.classic.content.explosive.ex.missiles;

import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.prefab.tile.EnumTier;

/** Ex object that are only defined as missiles
 *
 * @author Calclavia */
public abstract class Missile extends Explosion
{
    public Missile(String name, EnumTier tier)
    {
        super(name, tier);
        this.hasBlock = false;
        this.hasGrenade = false;
        this.hasMinecart = false;
    }
}
