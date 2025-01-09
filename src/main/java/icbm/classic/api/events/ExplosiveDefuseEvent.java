package icbm.classic.api.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when a player leftclicks an entity
 * that can be defused. Use specific events for
 * more control.
 *
 * @deprecated will be replaced with an event providing {@link icbm.classic.api.actions.IAction}
 */
@Deprecated
public class ExplosiveDefuseEvent extends Event
{
    public final PlayerEntity player;
    public final Entity entityToDefuse;

    public ExplosiveDefuseEvent(PlayerEntity player, Entity entityToDefuse)
    {
        this.player = player;
        this.entityToDefuse = entityToDefuse;
    }

    /**
     * Called when a player leftclicks a vanilla TNT block,
     * or potential derivates of other mods. Cancel to not
     * defuse the tnt.
     */
    @Cancelable
    public static class TNTExplosive extends ExplosiveDefuseEvent
    {
        public TNTExplosive(PlayerEntity player, Entity entityToDefuse)
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
        public ICBMBombCart(PlayerEntity player, Entity entityToDefuse)
        {
            super(player, entityToDefuse);
        }
    }
}
