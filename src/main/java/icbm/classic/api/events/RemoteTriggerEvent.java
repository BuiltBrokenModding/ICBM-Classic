package icbm.classic.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.Event;

/**
 * Called on the server side when the player rightclicks
 * the remote detonator. Use this to cancel the event to
 * not activate the affected launcher.
 */
public class RemoteTriggerEvent extends Event implements ICancellableEvent
{
    public final Level world;
    public final Player player;
    public final ItemStack stack;

    public RemoteTriggerEvent(Level world, Player player, ItemStack stack)
    {
        this.world = world;
        this.stack = stack;
        this.player = player;
    }
}
// PORTED
