package icbm.classic.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Called on the server side when the player rightclicks
 * the remote detonator. Use this to cancel the event to
 * not activate the affected launcher.
 */
public class RemoteTriggerEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final Player player;
    public final ItemStack stack;

    public RemoteTriggerEvent(Level level, Player player, ItemStack stack) {
        this.level = level;
        this.stack = stack;
        this.player = player;
    }
}
