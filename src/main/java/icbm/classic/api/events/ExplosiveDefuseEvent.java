package icbm.classic.api.events;


import icbm.classic.api.caps.IExplosive;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a player leftclicks an entity
 * that can be defused. Use specific events for
 * more control.
 */
public class ExplosiveDefuseEvent extends Event
{
    public EntityPlayer player;
    public Entity entityToDefuse;

    public ExplosiveDefuseEvent(EntityPlayer player, Entity entityToDefuse)
    {
        this.player = player;
        this.entityToDefuse = entityToDefuse;
    }

    /**
     * Called when a player leftclicks an ICBM explosive.
     * Cancel to not defuse the explosive.
     */
    @Cancelable
    public static class ICBMExplosive extends ExplosiveDefuseEvent
    {
        public IExplosive explosive;

        public ICBMExplosive(EntityPlayer player, Entity entityToDefuse, IExplosive explosive)
        {
            super(player, entityToDefuse);

            this.explosive = explosive;
        }
    }

    /**
     * Called when a player leftclicks a vanilla TNT block,
     * or potential derivates of other mods. Cancel to not
     * defuse the tnt.
     */
    @Cancelable
    public static class TNTExplosive extends ExplosiveDefuseEvent
    {
        public TNTExplosive(EntityPlayer player, Entity entityToDefuse)
        {
            super(player, entityToDefuse);
        }
    }

    /**
     * Called when a player leftclicks an ICBM bomb cart.
     * Cancel to not defuse the bomb cart.
     */
    @Cancelable
    public static class ICBMBombCart extends ExplosiveDefuseEvent
    {
        public ICBMBombCart(EntityPlayer player, Entity entityToDefuse)
        {
            super(player, entityToDefuse);
        }
    }
}
