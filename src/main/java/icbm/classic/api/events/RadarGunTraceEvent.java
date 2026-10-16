package icbm.classic.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.Event;

/**
 * Called on the server side when the player rightclicks
 * the radar gun. Use this to change the block position
 * that gets saved to the radar, or cancel the event to
 * not have any data saved.
 */
public class RadarGunTraceEvent extends Event implements ICancellableEvent
{
    public final Level world;
    public final Player player;
    public Vec3 pos;

    public RadarGunTraceEvent(Level world, Vec3 pos, Player player)
    {
        this.world = world;
        this.pos = pos;
        this.player = player;
    }
}
// PORTED
