package icbm.classic.config.util;

import icbm.classic.ICBMClassic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
public class BlockStateConfigListOld {
    // TODO once ContentBuilder Json system is published for MC replace this with JSON version to support programmatic and more complex entries
    //      entries to consider once JSON is allowed: time/date specific, conditional statements such as IF(MOD) IF(WORLD) IF(MATH) IF(GEO_AREA), block sets/lists

    // Constructor
    @Getter
    private final String name;
    private final Consumer<BlockStateConfigListOld> reloadCallback;

    // Temporary storage
    final List<String> mods = new ArrayList<>();
    final Map<String, List<Function<Block, Boolean>>> fuzzyBlockChecks = new HashMap<>();

    // Block lists
    final HashSet<BlockState> blockStates = new HashSet();
    final HashSet<Block> blocks = new HashSet();
    final Map<Block, List<Function<BlockState, Boolean>>> blockStateMatchers = new HashMap();

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
        // PORT: 1.12 never cleared these, so removed [prop] entries stayed active and reloads stacked duplicates
        blockStateMatchers.clear();
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
        BuiltInRegistries.BLOCK.forEach(block -> {

            // Mods
            final String modId = BuiltInRegistries.BLOCK.getKey(block).getNamespace();
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

    public boolean contains(BlockState state) {
        if (state == null) {
            return false;
        }
        if(blocks.contains(state.getBlock()) || blockStates.contains(state))
        {
            return true;
        }
        if(Optional.ofNullable(blockStateMatchers.get(state.getBlock()))
            .map(l -> l.stream().anyMatch(f -> f.apply(state))).orElse(false)) {
            blockStates.add(state);
            return true;
        }
        return false;
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
        if (checkLock("blocks", () -> String.join(", ", entries))) { //TODO trim string to not dump 1000s of lines into logs
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
            return handleSimpleBlock(entry);
        }
        // Catch all if something fails with block states in other mods
        catch (Exception e) {
            ICBMClassic.logger().error(name + ": Unexpected error parsing `" + entry + "` for banAllow list.", e);
        }
        return false;
    }

    boolean handleSimpleBlock(String entry) {
        final ResourceLocation blockKey = this.getBlockKey(entry);
        if(blockKey == null) {
            return false;
        }

        if(!BuiltInRegistries.BLOCK.containsKey(blockKey)) {
            ICBMClassic.logger().error(name + ": Failed to find block matching entry `" + entry + "`");
            return false;
        }

        return blocks.add(BuiltInRegistries.BLOCK.get(blockKey));
    }

    ResourceLocation getBlockKey(String entry) {
        // TODO convert to regex
        final String[] keySplit = entry.split(":", -1);
        final String domain = keySplit.length == 2 ? keySplit[0].trim() : null;
        final String key = keySplit.length == 2 ? keySplit[1].trim() : null;
        if (keySplit.length != 2 || domain.isEmpty() || key.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryBuild(domain.toLowerCase(java.util.Locale.ROOT), key.toLowerCase(java.util.Locale.ROOT)); // PORT: null if invalid in 1.21
    }

    boolean handleMetaData(String entry) {
        // PORT: block metadata does not exist in 1.21. Entries like `minecraft:stone@2` must be rewritten
        // using block properties, Ex: `minecraft:stone[variant:granite]` or as separate blocks `minecraft:granite`
        ICBMClassic.logger().error(name + ": Metadata entries are not supported anymore, `" + entry + "` was ignored. Use `mod:block[property:value]` instead.");
        return false;
    }

    boolean handleBlockState(String entry) {

        final String[] nameVsPropSplit = entry.split("\\[");

        // Block data
        final ResourceLocation regName = this.getBlockKey(nameVsPropSplit[0]);
        if (regName == null) {
            // TODO log formatting issue
            return false;
        }

        if (!BuiltInRegistries.BLOCK.containsKey(regName)) {
            ICBMClassic.logger().error("Config Flying Block: Failed to find block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
            return false;
        }
        final Block block = BuiltInRegistries.BLOCK.get(regName);

        // Properties
        final String[] properties = nameVsPropSplit[1].replace("]", "").split(",");

        final Map<Property, Function<Comparable, Boolean>> matchers = new HashMap();

        for (String propEntry : properties) {
            final String[] stateSplit = propEntry.split(":");
            final String propName = stateSplit[0].trim();
            final String propValue = stateSplit[1].trim();

            final Property property = block.getStateDefinition().getProperty(propName);
            if (property == null) {
                ICBMClassic.logger().error("Config Flying Block: Failed to find property '" + propName + "' for block '" + regName + "' matching entry `" + entry + "` for banAllow list.");
                return false;
            }

            if (propValue.equals("~")) {
                matchers.put(property, (o) -> true);
            } else if (propValue.startsWith("~")) {
                final String stringMatch = propValue.substring(1).trim();
                final List<Comparable<?>> valuesToMatch = (List<Comparable<?>>) property.getPossibleValues().stream()
                    .filter(o -> property.getName((Comparable) o).endsWith(stringMatch)).collect(Collectors.toList());
                if(valuesToMatch.isEmpty()) {
                    ICBMClassic.logger().error("Config Flying Block: Failed to find values matching '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "`");
                    return false;
                }
                matchers.put(property, valuesToMatch::contains);
            } else if (propValue.endsWith("~")) {
                final String stringMatch = propValue.substring(0, propValue.length() - 1).trim();
                final List<Comparable<?>> valuesToMatch = (List<Comparable<?>>) property.getPossibleValues().stream()
                    .filter(o -> property.getName((Comparable) o).startsWith(stringMatch)).collect(Collectors.toList());
                if(valuesToMatch.isEmpty()) {
                    ICBMClassic.logger().error("Config Flying Block: Failed to find values matching '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "`");
                    return false;
                }
                matchers.put(property, valuesToMatch::contains);
            } else {
                // Simple value matcher
                final Optional value = property.getPossibleValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
                if (!value.isPresent()) {
                    ICBMClassic.logger().error("Config Flying Block: Failed to find value '" + propValue + "' for property '" + propName + "' and block '" + regName + "' matching entry `" + entry + "`");
                    return false;
                }
                matchers.put(property, (o) -> Objects.equals(value.get(), o));
            }
        }

        if(matchers.isEmpty()) {
            return false;
        }

        if(!blockStateMatchers.containsKey(block)) {
            blockStateMatchers.put(block, new ArrayList());
        }
        return blockStateMatchers.get(block).add((blockState) -> matchesFuzzyState(blockState, matchers));
    }

    boolean matchesFuzzyState(BlockState state, Map<Property, Function<Comparable, Boolean>> matchers) {
        for(Property propKey: matchers.keySet()) {
            if(!state.hasProperty(propKey)) {
                return false;
            }

            final Function<Comparable, Boolean> check = matchers.get(propKey);
            if(check != null && !check.apply(state.getValue(propKey))) {
                return false;
            }
        }
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
        return BuiltInRegistries.BLOCK.getKey(block).getPath().startsWith(value);
    }

    boolean blockPathEndsWith(Block block, String value) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath().endsWith(value);
    }

    void addFuzzyForBlock(String domain, Function<Block, Boolean> check) {
        if (!this.fuzzyBlockChecks.containsKey(domain)) {
            this.fuzzyBlockChecks.put(domain, new ArrayList<>());
        }
        this.fuzzyBlockChecks.get(domain).add(check);
    }

    public List<String> dumpBlocksContained() {
        final List<String> list = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(block -> {
            if(this.contains(block.defaultBlockState())) {
                list.add(block.builtInRegistryHolder().key().location().toString());
            }
        });
        return list;
    }
}
// PORTED
