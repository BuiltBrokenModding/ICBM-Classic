package icbm.classic.api.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called on the server side when the player rightclicks
 * the radar gun. Use this to change the block position
 * that gets saved to the radar, or cancel the event to
 * not have any data saved.
 */
@Cancelable
public class RadarGunTraceEvent extends Event
{
    public final World world;
    public final EntityPlayer player;
    public BlockPos pos;

    public RadarGunTraceEvent(World world, BlockPos pos, EntityPlayer player)
    {
        this.world = world;
        this.pos = pos;
        this.player = player;
    }
}
