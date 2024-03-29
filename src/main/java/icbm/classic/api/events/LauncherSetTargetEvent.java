package icbm.classic.api.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the target of a launcher is set.
 * Use this to change the position that is being set,
 * or cancel the event to not set any position.
 */
@Cancelable
public class LauncherSetTargetEvent extends Event
{
    public final World world;
    public final BlockPos pos;
    public Vec3d target;

    public LauncherSetTargetEvent(World world, BlockPos pos, Vec3d target)
    {
        this.world = world;
        this.pos = pos;
        this.target = target;
    }
}
