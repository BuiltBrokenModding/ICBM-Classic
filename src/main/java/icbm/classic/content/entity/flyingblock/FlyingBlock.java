package icbm.classic.content.entity.flyingblock;

import icbm.classic.config.ConfigFlyingBlocks;
import icbm.classic.config.util.BlockStateConfigListOld;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlyingBlock {

    // Config list controlling if a block is allowed for spawning
    static final BlockStateConfigListOld banAllowList = new BlockStateConfigListOld("[Flying Blocks][Ban/Allow Config]",
        (blockStateConfigList) -> {
            // Mod blacklisted due to https://github.com/BuiltBrokenModding/ICBM-Classic/issues/420
            blockStateConfigList.addMod("dynamictrees");//TODO remove when issue #420 is resolved


            // Load configs
            blockStateConfigList.loadBlockStates(ConfigFlyingBlocks.banAllow.blockStates);
        }
    );

    /**
     * Validates if the given block state is allowed as a flying block
     *
     * @param state to check
     * @return true if allowed
     */
    public static boolean isAllowed(BlockState state) {
        if(!ConfigFlyingBlocks.enabled
            || state == null
            || state.getBlock() instanceof BaseFireBlock // 1.12 Material.FIRE
            || state.getBlock() instanceof LiquidBlock // 1.12 also checked Forge IFluidBlock
        ) {
            return false;
        }

        // Ban List
        if (ConfigFlyingBlocks.banAllow.ban) {
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
    public static BlockState applyMutations(BlockState state) {
        return state;
    }

    /**
     * Spawns a flying block, will check that block isn't air and isn't unbreakable
     * <p>
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param pos   to set
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(Level world, BlockPos pos,
                                           Consumer<EntityFlyingBlock> preSpawnCallback,
                                           Consumer<EntityFlyingBlock> postSpawnCallback) {
        final BlockState blockState = world.getBlockState(pos);
        if (!blockState.isAir() && isAllowed(blockState))
        {
            final float hardness = blockState.getDestroySpeed(world, pos);
            if (hardness >= 0)
            {
                final BlockCaptureData blockCaptureData = new BlockCaptureData(world, pos);
                return spawnFlyingBlock(world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    blockCaptureData, preSpawnCallback, postSpawnCallback,
                    () -> world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)
                );
            }
        }
        return false;
    }

    /**
     * Spawns a flying block, it is assumed the caller does any pre-checks on block itself.
     * <p>
     * If a mod wishes to prevent this use the forge entity spawn events.
     *
     * @param world to spawn into
     * @param x location
     * @param y location
     * @param z location
     * @param blockCaptureData to spawn, can be replaced by other systems and user config
     * @param removeBlock callback to set to air, done after collecting data
     * @return true if spawned
     */
    public static boolean spawnFlyingBlock(Level world, double x, double y, double z,
                                           BlockCaptureData blockCaptureData,
                                           Consumer<EntityFlyingBlock> preSpawnCallback,
                                           Consumer<EntityFlyingBlock> postSpawnCallback,
                                           Supplier<Boolean> removeBlock) {
        if (!isAllowed(blockCaptureData.getBlockState())) {
            return false;
        }

        if(removeBlock != null && !removeBlock.get()) {
            return false;
        }

        // TODO limit per chunk and per world to help reduce lag

        final EntityFlyingBlock flyingBlock = new EntityFlyingBlock(world);
        flyingBlock.setBlockData(blockCaptureData); //TODO allow mutations of state
        flyingBlock.setPos(x, y, z);

        // Pre-spawn data set, needed for extra properties that should be exposed to spawn event
        Optional.ofNullable(preSpawnCallback).ifPresent(f -> f.accept(flyingBlock));

        if (world.addFreshEntity(flyingBlock)) {

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
// PORTED
