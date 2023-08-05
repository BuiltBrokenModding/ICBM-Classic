package icbm.classic.content.entity.flyingblock;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigFlyingBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;

public class FlyingBlock {

    private static final List<String> disabledMods = Lists.newArrayList("dynamictrees");

    private static final HashSet<IBlockState> allowBanBlockStates = new HashSet();
    private static final HashSet<Block> allowBanBlocks = new HashSet();

    private static final HashSet<Block> buggedBlockList = new HashSet();

    public static boolean isAllowed(IBlockState state) {
        if (!ConfigFlyingBlocks.ENABLED || state == null || buggedBlockList.contains(state.getBlock())) {
            return false;
        }

        // Ban List
        if (ConfigFlyingBlocks.BAN_ALLOW.BAN) {
            return !allowBanBlocks.contains(state.getBlock()) && !allowBanBlockStates.contains(state);
        }

        // Allow List
        return allowBanBlocks.contains(state.getBlock()) || allowBanBlockStates.contains(state);
    }

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

        //TODO consider storing logic in a class for reuse between several configs. As this really is just a generic block lookup system.

        final List<String> mods =  new ArrayList();

        for(String str : ConfigFlyingBlocks.BAN_ALLOW.BLOCK_STATES) {
            final String entry = str.trim();

            try {
                // TODO replace with regex for better match detection
                if (entry.contains("@")) {
                    handleMetaData(entry);
                } else if (entry.contains("[")) {
                    handleBlockState(entry);
                } else if (entry.contains("~")) {
                    handleRange(entry);
                } else {
                    handleBlock(entry);
                }
            }
            // Catch all if something fails with block states in other mods
            catch (Exception e) {
                ICBMClassic.logger().error("Config Flying Block: Unexpected error parsing `" + entry + "` for banAllow list.", e);
            }
        }

        // Handle mods by looping entire registry
        ForgeRegistries.BLOCKS.forEach(block -> {

            // Mods
            final String modId = block.getRegistryName().getResourceDomain();
            if (disabledMods.contains(modId) || mods.contains(modId)) {
                buggedBlockList.add(block);
            }
        });

        //TODO load replacements, ensure we store as block -> handler
    }

    private static void handleBlock(String entry) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));
        if(block != null) {
            allowBanBlocks.add(block);
        }
        else {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block matching entry `" + entry + "` for banAllow list.");
        }
    }

    private static void handleMetaData(String entry) {
        final String[] split = entry.split("@");
        if(split.length != 2 || !split[1].matches("\\d+") || !split[0].contains(":")) {
            ICBMClassic.logger().error("Config Flying Block: Detected invalid metadata format for `" + entry + "`  for banAllow list. Expected `mod:key@number` example: `minecraft:stone@2`");
        }
        final ResourceLocation blockKey = new ResourceLocation(split[0]);
        final Block block = ForgeRegistries.BLOCKS.getValue(blockKey);
        final int metadata = Integer.parseInt(split[1]);
        if(block != null) {
           final IBlockState state = block.getStateFromMeta(metadata);
           if(state != null) {
               allowBanBlockStates.add(state);
           }
           else {
               ICBMClassic.logger().error("Config Flying Block: Failed to find state matching entry `" + entry + "` for banAllow list.");
           }
        }
        else {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block matching entry `" + entry + "` for banAllow list.");
        }
    }

    private static void handleBlockState(String entry) {
        //TODO need to store block -> state parser, as we may get partial properties rather than super specific
    }

    private static void handleRange(String entry) {

    }
}
