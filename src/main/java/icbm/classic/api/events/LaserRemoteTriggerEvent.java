package icbm.classic.api.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called on the server side when the player rightclicks
 * the laser detonator. Use this to change the target
 * block position, or cancel the event to not activate
 * the affected launcher.
 */
@Cancelable
public class LaserRemoteTriggerEvent extends Event
{
    public World world;
    public EntityPlayer player;
    public BlockPos pos;

    public LaserRemoteTriggerEvent(World world, BlockPos pos, EntityPlayer player)
    {
        this.world = world;
        this.pos = pos;
        this.player = player;
    }
}
