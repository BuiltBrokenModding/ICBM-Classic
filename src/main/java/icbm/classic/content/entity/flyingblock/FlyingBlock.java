package icbm.classic.content.entity.flyingblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

public class FlyingBlock {

    public static boolean isAllowed(IBlockState state) {
        return state != null;
    }

    /**
     * Spawns a flying block
     *
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param pos to set
     * @param state to spawn, can be replaced by other systems and user config
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(World world, BlockPos pos, IBlockState state) {
        return spawnFlyingBlock(world, pos, state, null, null);
    }
    /**
     * Spawns a flying block
     *
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param pos to set
     * @param state to spawn, can be replaced by other systems and user config
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(World world, BlockPos pos, IBlockState state,
                                           Consumer<EntityFlyingBlock> preSpawnCallback,
                                           Consumer<EntityFlyingBlock> postSpawnCallback) {
        if(!isAllowed(state)) {
            return false;
        }

        final EntityFlyingBlock flyingBlock = new EntityFlyingBlock(world);
        flyingBlock.setBlockState(state); //TODO allow mutations of state
        flyingBlock.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        // Pre-spawn data set, needed for extra properties that should be exposed to spawn event
        Optional.ofNullable(preSpawnCallback).ifPresent(f -> f.accept(flyingBlock));

        if(world.spawnEntity(flyingBlock)) {

            // Post-spawn data set, needed for logic that can't run outside the world
            Optional.ofNullable(postSpawnCallback).ifPresent(f -> f.accept(flyingBlock));

            //TODO add event logging
            return true;
        }
        return false;
    }
}
