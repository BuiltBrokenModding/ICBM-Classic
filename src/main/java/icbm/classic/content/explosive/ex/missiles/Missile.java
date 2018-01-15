package icbm.classic.content.explosive.ex.missiles;

import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.prefab.BlockICBM;

/** Ex object that are only defined as missiles
 *
 * @author Calclavia */
public abstract class Missile extends Explosion
{
    public Missile(String name, BlockICBM.EnumTier tier)
    {
        super(name, tier);
        this.hasBlock = false;
        this.hasGrenade = false;
        this.hasMinecart = false;
    }
}
