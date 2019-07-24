package icbm.classic.api.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called on the server side when the player rightclicks
 * the remote detonator. Use this to cancel the event to
 * not activate the affected launcher.
 */
@Cancelable
public class RemoteTriggerEvent extends Event
{
    public World world;
    public EntityPlayer player;
    public ItemStack stack;

    public RemoteTriggerEvent(World world, EntityPlayer player, ItemStack stack)
    {
        this.world = world;
        this.stack = stack;
        this.player = player;
    }
}
