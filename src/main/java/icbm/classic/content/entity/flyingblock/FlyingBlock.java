package icbm.classic.content.entity.flyingblock;

import icbm.classic.config.ConfigFlyingBlocks;
import icbm.classic.config.util.BlockStateConfigList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

public class FlyingBlock {

    // Config list controlling if a block is allowed for spawning
    private static final BlockStateConfigList banAllowList = new BlockStateConfigList("[Flying Blocks][Ban/Allow Config]",
        (blockStateConfigList) -> {
            // Mod blacklisted due to https://github.com/BuiltBrokenModding/ICBM-Classic/issues/420
            blockStateConfigList.addMod("dynamictrees"); //TODO remove when issue #420 is resolved

            // Load configs
            blockStateConfigList.loadBlockStates(ConfigFlyingBlocks.BAN_ALLOW.BLOCK_STATES);
        }
    );

    /**
     * Validates if the given block state is allowed as a flying block
     *
     * @param state to check
     * @return true if allowed
     */
    public static boolean isAllowed(IBlockState state) {
        if(!ConfigFlyingBlocks.ENABLED) {
            return false;
        }

        // Ban List
        if (ConfigFlyingBlocks.BAN_ALLOW.BAN) {
            return !banAllowList.contains(state);
        }

        // Allow List
        return  banAllowList.contains(state);
    }

    /**
     * Applies any mutations to the block state before converting to a flying block
     *
     * @param state to mutate
     * @return new state to use
     */
    public static IBlockState applyMutations(IBlockState state) {
        return state;
    }

    /**
     * Spawns a flying block
     * <p>
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param pos   to set
     * @param state to spawn, can be replaced by other systems and user config
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(World world, BlockPos pos, IBlockState state) {
        return spawnFlyingBlock(world, pos, state, null, null);
    }

    /**
     * Spawns a flying block
     * <p>
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param pos   to set
     * @param state to spawn, can be replaced by other systems and user config
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(World world, BlockPos pos, IBlockState state,
                                           Consumer<EntityFlyingBlock> preSpawnCallback,
                                           Consumer<EntityFlyingBlock> postSpawnCallback) {
        if (!isAllowed(state)) {
            return false;
        }

        // TODO limit per chunk and per world to help reduce lag

        final EntityFlyingBlock flyingBlock = new EntityFlyingBlock(world);
        flyingBlock.setBlockState(state); //TODO allow mutations of state
        flyingBlock.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        // Pre-spawn data set, needed for extra properties that should be exposed to spawn event
        Optional.ofNullable(preSpawnCallback).ifPresent(f -> f.accept(flyingBlock));

        if (world.spawnEntity(flyingBlock)) {

            // Post-spawn data set, needed for logic that can't run outside the world
            Optional.ofNullable(postSpawnCallback).ifPresent(f -> f.accept(flyingBlock));

            //TODO add event logging
            return true;
        }
        return false;
    }

    public static void loadFromConfig() {
        banAllowList.reload();
        //TODO load replacements, ensure we store as block -> handler
    }
}
