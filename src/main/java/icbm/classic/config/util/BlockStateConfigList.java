package icbm.classic.config.util;

import icbm.classic.ICBMClassic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * List of BlockStates/Blocks to use in config purposes. This is meant for internal use by the mod and should
 * never be touched by other mods. Use events, integrations, or ask for changes before bypassing this system.
 *
 * Life Cycle:
 * - unlock
 * - clear existing
 * - load(external inputs)
 * - batch(block-registry)
 * - lock
 */
@RequiredArgsConstructor
public class BlockStateConfigList {
    // TODO once ContentBuilder Json system is published for MC replace this with JSON version to support programmatic and more complex entries
    //      entries to consider once JSON is allowed: time/date specific, conditional statements such as IF(MOD) IF(WORLD) IF(MATH) IF(GEO_AREA), block sets/lists

    // Constructor
    @Getter
    private final String name;
    private final Consumer<BlockStateConfigList> reloadCallback;

    // Temporary storage
    final List<String> mods = new ArrayList<>();
    final Map<String, List<Function<Block, Boolean>>> fuzzyBlockChecks = new HashMap<>();

    // Block lists
    static final HashSet<IBlockState> blockStates = new HashSet();
    static final HashSet<Block> blocks = new HashSet();

    // States
    @Getter
    private boolean isLocked = false;

    public void reload(){
        this.unlock();
        this.reset();
        // Let the parent load defaults and pull in configs
        this.reloadCallback.accept(this);
        this.batchBlockRegistry();
        this.lock();
    }

    private void unlock() {
        this.isLocked = false;
    }

    private void reset() {
        blockStates.clear();
        blocks.clear();
    }

    private void lock() {
        this.isLocked = true;

        // Clear temp data
        mods.clear();
        fuzzyBlockChecks.clear();
    }

    public void batchBlockRegistry() {
        if(isLocked) {
            ICBMClassic.logger().error(name + ": list is locked but loading was invoked!!", new IllegalArgumentException());
            return;
        }

        // Handle mods by looping entire registry
        ForgeRegistries.BLOCKS.forEach(block -> {

            // Mods
            final String modId = block.getRegistryName().getResourceDomain();
            if (mods.contains(modId)) {
                blocks.add(block);
            }

            // Blocks generic fuzzy, allows for any logic to be run per block
            if (fuzzyBlockChecks.containsKey(modId)) {
                if (fuzzyBlockChecks.get(modId).stream().anyMatch(func -> func.apply(block))) {
                    blocks.add(block);
                }
            }
        });

        lock();
    }

    public boolean contains(IBlockState state) {
        if (state == null) {
            return false;
        }
        return blocks.contains(state.getBlock()) || blockStates.contains(state);
    }

    /**
     * Loads block states from a collection of strings. Strings can contain nearly anything
     * so long as it results in block(s) or block-state(s).
     *
     * @param entries to process
     */
    public void loadBlockStates(Iterable<String> entries) {
        if (checkLock("blocks", () -> String.join(", ", entries))) {
            return;
        }

        entries.forEach((str) -> {
            final String entry = str.trim();
            handleEntry(entry);
        });
    }

    /**
     * Loads block states from a collection of strings. Strings can contain nearly anything
     * so long as it results in block(s) or block-state(s).
     *
     * @param entries to process
     */
    public void loadBlockStates(String... entries) {
        if (checkLock("blocks", () -> String.join(", ", entries))) {
            return;
        }

        for (String str : entries) {
            final String entry = str.trim();
            handleEntry(entry);
        }
    }

    public void addMod(String mod) {
        if (checkLock("mod", () -> mod)) {
            return;
        }
        this.mods.add(mod);
    }

    private boolean checkLock(String type, Supplier<String> entry) {
        if (this.isLocked) {
            ICBMClassic.logger().error(name + ": list is locked. Unable to add '" + type + "' entry '" + entry.get() + "'", new IllegalArgumentException());
            return true;
        }
        return false;
    }

    void handleEntry(String entry) {
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
            // Blocks with fuzzy checks
            else if (entry.contains("~")) {
                handleFuzzyBlocks(entry);
            } else {
                handleBlock(entry);
            }
        }
        // Catch all if something fails with block states in other mods
        catch (Exception e) {
            ICBMClassic.logger().error(name + ": Unexpected error parsing `" + entry + "` for banAllow list.", e);
        }
    }

    void handleBlock(String entry) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));
        if (block != null) {
            blocks.add(block);
        } else {
            ICBMClassic.logger().error(name + ": Failed to find block matching entry `" + entry + "` for banAllow list.");
        }
    }

    void handleMetaData(String entry) {
        final String[] split = entry.split("@");
        if (split.length != 2 || !split[1].matches("\\d+") || !split[0].contains(":")) {
            ICBMClassic.logger().error(name + ": Detected invalid metadata format for `" + entry + "`  for banAllow list. Expected `mod:key@number` example: `minecraft:stone@2`");
        }
        final ResourceLocation blockKey = new ResourceLocation(split[0]);
        final Block block = ForgeRegistries.BLOCKS.getValue(blockKey);
        final int metadata = Integer.parseInt(split[1]);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(metadata);
            if (state != null) {
                blockStates.add(state);
            } else {
                ICBMClassic.logger().error(name + ": Failed to find state matching entry `" + entry + "` for banAllow list.");
            }
        } else {
            ICBMClassic.logger().error(name + ": Failed to find block matching entry `" + entry + "` for banAllow list.");
        }
    }

    void handleBlockState(String entry) {
        //TODO need to store block -> state parser, as we may get partial properties rather than super specific
        String[] split = entry.split("\\[");
        String[] split2 = split[0].split(":");

        // Block data
        final String domain = split2[0].trim();
        final String key = split2[1].trim();
        final ResourceLocation regName = new ResourceLocation(domain, key);

        final Block block = ForgeRegistries.BLOCKS.getValue(regName);

        if (block == null) {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
            return;
        }

        // Properties
        final String[] properties = split[1].replace("]", "").split(",");

        Map<IProperty, Function<Comparable, Boolean>> matchers = new HashMap();

        for (String propEntry : properties) {
            final String[] split3 = propEntry.split(":");
            final String propName = split3[0].trim();
            final String propValue = split[1].trim();

            final IProperty property = block.getBlockState().getProperty(propName);
            if (property == null) {
                ICBMClassic.logger().error("Config Flying Block: Failed to find property '" + propName + "' for block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                return;
            }

            if (propValue.equals("~")) {
                matchers.put(property, (o) -> true);
            } else if (propValue.startsWith("~")) {
                // TODO fuzz matcher
            } else if (propValue.endsWith("~")) {
                // TODO fuzz matcher
            }
            // Simple value matcher
            else {
                final Optional value = property.getAllowedValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
                if (!value.isPresent()) {
                    ICBMClassic.logger().error("Config Flying Block: Failed to find value '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                    return;
                }
                matchers.put(property, (o) -> Objects.equals(value.get(), o));
            }
        }
    }

    void handleFuzzyBlocks(String entry) {
        final String[] split = entry.split(":");

        //FORMAT CHECK: Requires single ':' and should contain only a single '~'
        if (split.length != 2 || split[1].lastIndexOf("~") != split[1].indexOf("~")) {
            ICBMClassic.logger().error(name + ": Detected invalid fuzzy format for `" + entry + "`  for banAllow list. Expected `mod:key~`, `mod:~` or `mod:key[prop:~]`");
            return;
        }

        final String domain = split[0].trim();
        final String resource = split[1].trim();

        // case 1: General mod addition, technically a fuzzy check but really is a modID==value for sanity reasons
        if (Objects.equals(resource, "~")) {
            mods.add(domain);
        }
        // case 2: fuzzy leading word check, Ex: `minecraft:iron~` would match all iron blocks such as `minecraft:iron_bars`
        else if (resource.endsWith("~")) {
            addFuzzyForBlock(domain, (block) -> blockPathStartsWith(block, resource.substring(0, resource.length() - 1)));
        }
        // case 3: fuzzy trailing word check, EX: `minecraft:~door` would match all doors such as `minecraft:oak_door`
        else if (resource.startsWith("~")) {
            addFuzzyForBlock(domain, (block) -> blockPathEndsWith(block, resource.substring(1)));
        }
    }

    boolean blockPathStartsWith(Block block, String value) {
        return Objects.requireNonNull(block.getRegistryName()).getResourcePath().startsWith(value);
    }

    boolean blockPathEndsWith(Block block, String value) {
        return Objects.requireNonNull(block.getRegistryName()).getResourcePath().endsWith(value);
    }

    void addFuzzyForBlock(String domain, Function<Block, Boolean> check) {
        if(!this.fuzzyBlockChecks.containsKey(domain)) {
            this.fuzzyBlockChecks.put(domain, new ArrayList<>());
        }
        this.fuzzyBlockChecks.get(domain).add(check);
    }
}
