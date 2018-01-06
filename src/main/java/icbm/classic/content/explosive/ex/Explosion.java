package icbm.classic.content.explosive.ex;

import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import net.minecraft.entity.player.EntityPlayer;

public abstract class Explosion extends Explosive
{
    public Explosion(String name, int tier)
    {
        super(name, tier);
    }

    /** Called when launched. */
    public void launch(EntityMissile missileObj)
    {
    }

    /** Called every tick while flying. */
    public void update(EntityMissile missileObj)
    {
    }

    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer)
    {
        return false;
    }

    /**
     * Is this missile compatible with the cruise launcher?
     *
     * @return
     */
    public boolean isCruise()
    {
        return true;
    }
}
