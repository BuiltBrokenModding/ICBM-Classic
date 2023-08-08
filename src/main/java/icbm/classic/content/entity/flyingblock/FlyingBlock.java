package icbm.classic.content.entity.flyingblock;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeBasedTable;
import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigFlyingBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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

        final List<String> mods = new ArrayList<>();
        final Map<String, List<String>> startsWith = new HashMap<>();
        final Map<String, List<String>> endsWith = new HashMap<>();

        for(String str : ConfigFlyingBlocks.BAN_ALLOW.BLOCK_STATES) {
            final String entry = str.trim();

            try {
                // TODO replace with regex for better match detection

                // Metadata sugar for 1.12
                if (entry.contains("@")) {
                    handleMetaData(entry);
                }
                // Block states, also supports ~
                else if (entry.contains("[")) {
                    handleBlockState(entry);
                }
                // Range of blocks or all from a mod
                else if (entry.contains("~")) {
                    handleRange(entry, mods::add, (domain, key, start) -> {
                        if(start) {
                            if(!startsWith.containsKey(domain)) {
                                startsWith.put(domain, new ArrayList<>());
                            }
                            startsWith.get(domain).add(key);
                        }
                        else {
                            if(!endsWith.containsKey(domain)) {
                                endsWith.put(domain, new ArrayList<>());
                            }
                            endsWith.get(domain).add(key);
                        }
                    });
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

            // Blocks
            final String key = block.getRegistryName().getResourcePath();
            if(startsWith.containsKey(modId)) {
                final List<String> values = startsWith.get(modId);
                if(values.stream().anyMatch(key::startsWith)) {
                    allowBanBlocks.add(block);
                }
            }
            else if(endsWith.containsKey(key)) {
                final List<String> values = endsWith.get(modId);
                if(values.stream().anyMatch(key::endsWith)) {
                    allowBanBlocks.add(block);
                }
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
        String[] split =  entry.split("\\[");
        String[] split2 = split[0].split(":");

        // Block data
        final String domain = split2[0].trim();
        final String key = split2[1].trim();
        final ResourceLocation regName = new ResourceLocation(domain, key);

        final Block block = ForgeRegistries.BLOCKS.getValue(regName);

        if(block == null) {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
            return;
        }

        // Properties
        final String[] properties = split[1].replace("]", "").split(",");

        Map<IProperty, Function<Comparable, Boolean>> matchers = new HashMap();

        for(String propEntry : properties) {
            final String[] split3 = propEntry.split(":");
            final String propName = split3[0].trim();
            final String propValue = split[1].trim();

            final IProperty property = block.getBlockState().getProperty(propName);
            if(property == null) {
                ICBMClassic.logger().error("Config Flying Block: Failed to find property '" + propName + "' for block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                return;
            }

            if(propValue.contains("~")) {
                // TODO fuzz matcher
            }
            else {
                Optional<Comparable> value = property.getAllowedValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
                if(!value.isPresent()) {
                    ICBMClassic.logger().error("Config Flying Block: Failed to find value '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                    return;
                }
                matchers.put(property, (o) -> Objects.equals(value.get(), o));
                // TODO simple matcher
            }
        }
    }

    private static void handleRange(String entry, Consumer<String> mods, TriConsumer<String, String, Boolean> startEndConsumer) {
        final String[] split = entry.split(":");
        if(split.length != 2) {
            ICBMClassic.logger().error("Config Flying Block: Detected invalid range format for `" + entry + "`  for banAllow list. Expected `mod:key~`, `mod:~` or `mod:key[prop:~]`");
        }

        final String domain = split[0].trim();
        final String resource = split[1].trim();

        // case 1: all from mod
        if(Objects.equals(resource, "~")) {
            mods.accept(domain);
        }

        // case 3: all from a block name similarity
        else if(resource.startsWith("~")) {
            startEndConsumer.accept(domain, resource.substring(1), true);
        }
        else if(resource.endsWith("~")) {
            startEndConsumer.accept(domain, resource.substring(0, resource.length() - 1), false);
        }
    }
}
