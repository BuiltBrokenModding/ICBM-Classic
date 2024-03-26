package icbm.classic.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nonnull;

/**
 * Called on the server side when the player rightclicks
 * the laser detonator. Use this to change the target
 * block position, or cancel the event to not activate
 * the affected launcher.
 */
public class LaserRemoteTriggerEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final Player player;
    private Vec3 pos;

    /**
     * Optional translation key to show user for why it was canceled
     */
    public String cancelReason;

    public LaserRemoteTriggerEvent(Level level, Vec3 pos, Player player) {
        this.level = level;
        this.pos = pos;
        this.player = player;
    }

    /**
     * Updates the target position of the event
     *
     * @param pos to set, can't be null or will throw exception
     */
    public void setPos(@Nonnull Vec3 pos) {
        if (pos == null) {
            throw new IllegalArgumentException("LaserRemoteTriggerEvent: target pos can not be set to null");
        }
        this.pos = pos;
    }

    public Vec3 getPos() {
        return pos;
    }
}
