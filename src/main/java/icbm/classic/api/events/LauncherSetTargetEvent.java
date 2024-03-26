package icbm.classic.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Called when the target of a launcher is set.
 * Use this to change the position that is being set,
 * or cancel the event to not set any position.
 */
public class LauncherSetTargetEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final BlockPos pos;
    public Vec3 target;

    public LauncherSetTargetEvent(Level level, BlockPos pos, Vec3 target) {
        this.level = level;
        this.pos = pos;
        this.target = target;
    }
}
