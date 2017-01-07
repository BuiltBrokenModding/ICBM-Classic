package icbm.classic.content.explosive.ex.missiles;

import icbm.classic.content.explosive.ex.Explosion;

/** Ex object that are only defined as missiles
 *
 * @author Calclavia */
public abstract class Missile extends Explosion
{
    public Missile(String name, int tier)
    {
        super(name, tier);
        this.hasBlock = false;
        this.hasGrenade = false;
        this.hasMinecart = false;
    }
}
