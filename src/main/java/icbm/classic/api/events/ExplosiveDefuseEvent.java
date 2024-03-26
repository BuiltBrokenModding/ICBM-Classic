package icbm.classic.api.events;


import icbm.classic.api.caps.IExplosive;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Called when a player leftclicks an entity
 * that can be defused. Use specific events for
 * more control.
 */
public class ExplosiveDefuseEvent extends Event {
    public final Player player;
    public final Entity entityToDefuse;

    public ExplosiveDefuseEvent(Player player, Entity entityToDefuse) {
        this.player = player;
        this.entityToDefuse = entityToDefuse;
    }

    /**
     * Called when a player leftclicks an ICBM explosive.
     * Cancel to not defuse the explosive.
     */
    public static class ICBMExplosive extends ExplosiveDefuseEvent implements ICancellableEvent {
        public final IExplosive explosive;

        public ICBMExplosive(Player player, Entity entityToDefuse, IExplosive explosive) {
            super(player, entityToDefuse);

            this.explosive = explosive;
        }
    }

    /**
     * Called when a player leftclicks a vanilla TNT block,
     * or potential derivates of other mods. Cancel to not
     * defuse the tnt.
     */
    public static class TNTExplosive extends ExplosiveDefuseEvent implements ICancellableEvent {
        public TNTExplosive(Player player, Entity entityToDefuse) {
            super(player, entityToDefuse);
        }
    }

    /**
     * Called when a player leftclicks an ICBM bomb cart.
     * Cancel to not defuse the bomb cart.
     */
    public static class ICBMBombCart extends ExplosiveDefuseEvent implements ICancellableEvent {
        public ICBMBombCart(Player player, Entity entityToDefuse) {
            super(player, entityToDefuse);
        }
    }
}
