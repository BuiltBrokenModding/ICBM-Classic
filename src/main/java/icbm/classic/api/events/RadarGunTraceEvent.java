package icbm.classic.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Called on the server side when the player rightclicks
 * the radar gun. Use this to change the block position
 * that gets saved to the radar, or cancel the event to
 * not have any data saved.
 */
public class RadarGunTraceEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final Player player;
    public Vec3 pos;

    public RadarGunTraceEvent(Level level, Vec3 pos, Player player) {
        this.level = level;
        this.pos = pos;
        this.player = player;
    }
}
