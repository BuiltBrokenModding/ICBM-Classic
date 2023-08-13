package icbm.classic.config.util;

import icbm.classic.ICBMClassic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * List of BlockStates/Blocks to use in config purposes. This is meant for internal use by the mod and should
 * never be touched by other mods. Use events, integrations, or ask for changes before bypassing this system.
 * <p>
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
    final HashSet<IBlockState> blockStates = new HashSet();
    final HashSet<Block> blocks = new HashSet();

    // States
    @Getter
    private boolean isLocked = false;

    public void reload() {
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
        if (isLocked) {
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

    boolean handleEntry(String entry) {
        try {
            // TODO replace with regex for better match detection
            // TODO remove spaces from entries so we don't need to do string.trim() everywhere, really don't care if `m i n e c r a f t : s t o    n e' is valid as a result

            // Metadata sugar for 1.12
            if (entry.contains("@")) {
                return handleMetaData(entry);
            }
            // Block states, also supports ~
            else if (entry.contains("[")) {
                return handleBlockState(entry);
            }
            // Blocks with fuzzy checks
            else if (entry.contains("~")) {
                return handleFuzzyBlocks(entry);
            }
            //TODO add ore-dictionary using `@ore:` likely can do keywords using `@word` such as `@contains:` or `@regex:`
            return handleBlock(entry);
        }
        // Catch all if something fails with block states in other mods
        catch (Exception e) {
            ICBMClassic.logger().error(name + ": Unexpected error parsing `" + entry + "` for banAllow list.", e);
        }
        return false;
    }

    boolean handleBlock(String entry) {
        final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));
        if (block != null) {
            return blocks.add(block);
        }
        ICBMClassic.logger().error(name + ": Failed to find block matching entry `" + entry + "` for banAllow list.");
        return false;
    }

    ResourceLocation getBlockKey(String entry) {
        // TODO convert to regex
        final String[] keySplit = entry.split(":", -1);
        final String domain = keySplit.length == 2 ? keySplit[0].trim() : null;
        final String key = keySplit.length == 2 ? keySplit[1].trim() : null;
        if (keySplit.length != 2 || domain.isEmpty() || key.isEmpty()) {
            return null;
        }
        return new ResourceLocation(domain, key);
    }

    boolean handleMetaData(String entry) {
        // TODO replace validation sections with regex for cleaner extraction and linting

        // Validate general format
        final String[] metaSplit = entry.split("@");
        if (metaSplit.length != 2 || !metaSplit[1].matches("\\d+") || !metaSplit[0].contains(":")) {
            ICBMClassic.logger().error(name + ": Detected invalid metadata format for `" + entry + "`  for banAllow list. Expected `mod:key@number` example: `minecraft:stone@2`");
            return false; //TODO maybe throw instead so we can unit test the errors?
        }

        // Validate key format
        final ResourceLocation blockKey = this.getBlockKey(metaSplit[0]);
        if (blockKey == null) {
            ICBMClassic.logger().error(name + ": Detected invalid metadata format for `" + entry + "`  for banAllow list. Expected `mod:key@number` example: `minecraft:stone@2`");
            return false;
        }

        // Get block from provided key
        if (!ForgeRegistries.BLOCKS.containsKey(blockKey)) {
            ICBMClassic.logger().error(name + ": Failed to find block matching entry `" + entry + "` for banAllow list.");
            return false;
        }
        final Block block = ForgeRegistries.BLOCKS.getValue(blockKey);

        // Get state from meta value
        final int desiredMetadata = Integer.parseInt(metaSplit[1]);
        final IBlockState state = block.getStateFromMeta(desiredMetadata);

        // Null state is a sign of a buggy mod-block
        if (state == null) {
            ICBMClassic.logger().error(name + ": Failed to find state matching entry `" + entry + "` for banAllow list. This is a bug in '" + blockKey.getResourceDomain() + "'!");
            return false;
        }

        // Validate metadata, default implementation is to return meta value of 0 for unknowns
        final int metaActual = block.getMetaFromState(state);
        if (desiredMetadata != metaActual) {
            ICBMClassic.logger().error(name + ": Block returned a state with metadata[" + metaActual + "] but it didn't match metadata[" + desiredMetadata + "] for entry `" + entry + "` for banAllow list.");
            return false;
        }

        return blockStates.add(state);
    }

    boolean handleBlockState(String entry) {

        String[] split = entry.split("\\[");

        // Block data
        final ResourceLocation regName = this.getBlockKey(split[0]);
        if(regName == null) {
            // TODO log formatting issue
            return false;
        }

        final Block block = ForgeRegistries.BLOCKS.getValue(regName);

        if (block == null) {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
            return false;
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
                return false;
            }

            if (propValue.equals("~")) {
                matchers.put(property, (o) -> true);
            } else if (propValue.startsWith("~")) {
                // TODO fuzz matcher
            } else if (propValue.endsWith("~")) {
                // TODO fuzz matcher
            }


            // Simple value matcher
            final Optional value = property.getAllowedValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
            if (!value.isPresent()) {
                ICBMClassic.logger().error("Config Flying Block: Failed to find value '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                return false;
            }
            matchers.put(property, (o) -> Objects.equals(value.get(), o));
        }

        //TODO need to store block -> state parser, as we may get partial properties rather than super specific
        return true;
    }

    boolean handleFuzzyBlocks(String entry) {
        final String[] split = entry.split(":");

        // TODO replace with regex
        //FORMAT CHECK: Requires single ':' and should contain only a single '~'
        if (split.length != 2 || split[1].lastIndexOf("~") != split[1].indexOf("~") || split[0].contains("~") || split[0].isEmpty()) {
            ICBMClassic.logger().error(name + ": Detected invalid fuzzy format for `" + entry + "`  for banAllow list. Expected `mod:~`, `mod:key~` or `mod:~key`");
            return false;
        }

        // TODO add fuzzy without domain such as `~stone`

        final String domain = split[0].trim();
        final String resource = split[1].trim();

        // case 1: General mod addition, technically a fuzzy check but really is a modID==value for sanity reasons
        if (Objects.equals(resource, "~")) {
            return mods.add(domain);
        }
        // case 2: fuzzy leading word check, Ex: `minecraft:iron~` would match all iron blocks such as `minecraft:iron_bars`
        else if (resource.endsWith("~")) {
            addFuzzyForBlock(domain, (block) -> blockPathStartsWith(block, resource.substring(0, resource.length() - 1)));
            return true;
        }
        // case 3: fuzzy trailing word check, EX: `minecraft:~door` would match all doors such as `minecraft:oak_door`
        else if (resource.startsWith("~")) {
            addFuzzyForBlock(domain, (block) -> blockPathEndsWith(block, resource.substring(1)));
            return true;
        }

        ICBMClassic.logger().error(name + ": Couldn't match fuzzy format for `" + entry + "`  for banAllow list. Expected `mod:~`, `mod:key~` or `mod:~key`");
        return false;
    }

    boolean blockPathStartsWith(Block block, String value) {
        return Objects.requireNonNull(block.getRegistryName()).getResourcePath().startsWith(value);
    }

    boolean blockPathEndsWith(Block block, String value) {
        return Objects.requireNonNull(block.getRegistryName()).getResourcePath().endsWith(value);
    }

    void addFuzzyForBlock(String domain, Function<Block, Boolean> check) {
        if (!this.fuzzyBlockChecks.containsKey(domain)) {
            this.fuzzyBlockChecks.put(domain, new ArrayList<>());
        }
        this.fuzzyBlockChecks.get(domain).add(check);
    }
}
