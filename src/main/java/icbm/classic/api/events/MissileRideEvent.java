package icbm.classic.api.events;

import icbm.classic.api.missiles.IMissile;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class MissileRideEvent extends Event {
    public final IMissile missile;
    public final Player player;

    public MissileRideEvent(IMissile missile, Player player) {
        this.missile = missile;
        this.player = player;
    }

    /**
     * Called right before a player starts to ride a missile.
     * Cancel this event to disallow the player to ride the missile.
     */
    public static class Start extends MissileRideEvent implements ICancellableEvent {
        public Start(IMissile missile, Player player) {
            super(missile, player);
        }
    }

    /**
     * Called right before a player stops to ride a missile.
     * Cancel this event to disallow the player to dismount the missile.
     */
    public static class Stop extends MissileRideEvent implements ICancellableEvent {
        public Stop(IMissile missile, Player player) {
            super(missile, player);
        }
    }
}
