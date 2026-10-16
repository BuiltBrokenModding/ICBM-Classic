package icbm.classic.config.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * List of BlockStates/Blocks to use in config purposes. This is meant for internal use by the mod and should
 * never be touched by other mods. Use events, integrations, or ask for changes before bypassing this system.
 */
public abstract class BlockStateConfigList<VALUE> extends ResourceConfigList<BlockStateConfigList<VALUE>, BlockState, VALUE> {

    // PORT: the domain group excludes ':', '=' and '['. In 1.12 `(.*)` caught `a:b=c:d[p:v]` as key `a:b=c` with props from the value
    protected static final Pattern BLOCK_PROP_REGEX = Pattern.compile("^([^\\s@:=\\[]*):([^=\\s\\[]*)\\[((?:[\\w.]+:[\\w.]+,?)+)\\](?:=(.*))?");
    // PORT: 1.12 `([0-9]*[.][0-9]*)` accepted `@chance(.)` which threw parsing, and rejected `@chance(1)`
    protected static final Pattern CHANCE_REGEX = Pattern.compile("^@chance\\(([0-9]*\\.?[0-9]+)\\)$");
    protected static final Pattern NBT_REGEX = Pattern.compile("^@nbt\\((.*)\\)$");


    public BlockStateConfigList(String name, Consumer<BlockStateConfigList<VALUE>> reloadCallback) {
        super(name, "https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states", reloadCallback);
        addMatcher(BLOCK_PROP_REGEX, this::handleBlockProps);
        addMatcher(META_KEY_REGEX, this::handleMeta);
        addMatcher(KEY_VALUE_REGEX, this::handleSimple);
    }

    @Override
    protected ResourceLocation getContentKey(BlockState iBlockState) {
        return BuiltInRegistries.BLOCK.getKey(iBlockState.getBlock());
    }

    @Override
    protected Function<BlockState, VALUE> getMetaValue(ResourceLocation key, int metadata, @Nullable VALUE value) {
        // PORT: block metadata no longer exists in 1.21, entries must use block properties `mod:block[prop:value]`
        return null;
    }

    protected Function<BlockState, VALUE> getPropValue(ResourceLocation key, Map<Property, Function<Comparable, Boolean>> propMatchers, @Nullable VALUE value) {
        return (blockState) -> {
            if (key.equals(getContentKey(blockState))) {
                for(Property propKey: propMatchers.keySet()) {
                    if(!blockState.hasProperty(propKey)) {
                        return null;
                    }

                    final Function<Comparable, Boolean> check = propMatchers.get(propKey);
                    if(check != null && !check.apply(blockState.getValue(propKey))) {
                        return null;
                    }
                }
                return value;
            }
            return null;
        };
    }

    protected ResourceConfigEntry<BlockState, VALUE> handleBlockProps(Matcher domainMatcher, String source, String entry, Integer index) { // PORT: was int, unboxing a null index threw NPE
        final String domain = domainMatcher.group(1);
        final String resource = domainMatcher.group(2);
        final ResourceLocation key = toKey(domain, resource);

        if (key == null) {
            error(source, entry, "Invalid resource location '" + domain + ":" + resource + "'");
            return null;
        } else if (!isDomainValid(key.getNamespace())) {
            error(source, entry, "No matching mod domain found for '" + key + "'");
            return null;
        } else if (!isValidKey(key)) {
            error(source, entry, "No matching content found for '" + key + "'");
            return null;
        }

        final Block block = BuiltInRegistries.BLOCK.get(key);
        final String[] props = domainMatcher.group(3).split(",");
        final Map<Property, Function<Comparable, Boolean>> propMatchers = collectBlockProps(source, entry, block, props);
        if (propMatchers == null) {
            return null;
        }

        final String valueStr = domainMatcher.group(4);
        // PORT: 1.12 parsed a missing value (`mod:block[prop:value]`) as null, which threw in BlockChanceOut. Matches handleSimple now.
        VALUE value = null;
        if (valueStr != null) {
            value = parseValue(source, entry, valueStr);
            if (value == null) {
                return null;
            }
        }

        final Function<BlockState, VALUE> matcher = getPropValue(key, propMatchers, value);
        return new ResourceConfigEntry<>("resource_block_state", index, matcher);
    }

    @Override
    protected boolean isValidKey(ResourceLocation targetKey) {
        return BuiltInRegistries.BLOCK.containsKey(targetKey);
    }

    protected Map<Property, Function<Comparable, Boolean>> collectBlockProps(String source, String entry, Block block, String[] props) {
        final Map<Property, Function<Comparable, Boolean>> matchers = new HashMap();
        for (String propEntry : props) {
            final Matcher propMatcher = KEY_VALUE_REGEX.matcher(propEntry);
            if (!propMatcher.matches()) {
                // TODO error log
                return null;
            }
            final String propKey = propMatcher.group(1);
            final String propValue = propMatcher.group(2);
            //TODO consider using group 3 as a boolean contains check

            final Property property = block.getStateDefinition().getProperty(propKey);
            if (property == null) {
                error(source, entry, String.format("Failed to find property '%s' for block '%s' matching entry `%s`", propKey, block.builtInRegistryHolder().key().location(), propEntry));
                return null;
            }

            if (propValue.equals("~")) {
                matchers.put(property, (o) -> true);
            } else if (propValue.startsWith("~") || propValue.endsWith("~")) {
                final String stringMatch = propValue.substring(1).trim();
                final List<Comparable<?>> valuesToMatch = (List<Comparable<?>>) property.getPossibleValues().stream()
                    .filter(o -> {
                        if (propValue.endsWith("~")) {
                            return property.getName((Comparable) o).endsWith(stringMatch);
                        }
                        return property.getName((Comparable) o).startsWith(stringMatch);
                    }).collect(Collectors.toList());
                if (valuesToMatch.isEmpty()) {
                    error(source, entry, String.format("Failed to find values matching '%s' for property '%s' and block '%s' matching entry '%s'", propValue, propKey, block.builtInRegistryHolder().key().location(), propEntry));
                    return null;
                }
                matchers.put(property, valuesToMatch::contains);
            } else {
                // Simple value matcher
                final Optional value = property.getPossibleValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
                if (!value.isPresent()) {
                    error(source, entry, String.format("Failed to find values matching '%s' for property '%s' and block '%s' matching entry '%s'", propValue, propKey, block.builtInRegistryHolder().key().location(), propEntry));
                    return null;
                }
                matchers.put(property, (o) -> Objects.equals(value.get(), o));
            }
        }
        return matchers;
    }


    protected BlockState parseBlockState(String source, String entry, String valueToParse) {
        final Matcher metaMatcher = META_KEY_REGEX.matcher(valueToParse);
        if (metaMatcher.matches()) {
            // PORT: metadata states (`mod:block@meta`) do not exist in 1.21
            error(source, entry, "Metadata is not supported, use block properties instead of '" + valueToParse + "'");
            return null;
        }

        final Matcher propMatcher = BLOCK_PROP_REGEX.matcher(valueToParse);
        if (propMatcher.matches() && propMatcher.group(4) == null) {
            // PORT: replaces `mod:block@meta` outputs, Ex: `minecraft:stone[variant:granite]` -> state with those properties
            final ResourceLocation key = toKey(propMatcher.group(1), propMatcher.group(2));
            if (key == null || !isDomainValid(key.getNamespace()) || !isValidKey(key)) {
                error(source, entry, "No matching content found for '" + valueToParse + "'");
                return null;
            }
            BlockState state = BuiltInRegistries.BLOCK.get(key).defaultBlockState();
            for (String propEntry : propMatcher.group(3).split(",")) {
                final String[] split = propEntry.split(":");
                final Property property = state.getBlock().getStateDefinition().getProperty(split[0]);
                if (property == null || split.length != 2) {
                    error(source, entry, String.format("Failed to find property '%s' for block '%s'", propEntry, key));
                    return null;
                }
                final Optional value = property.getValue(split[1].toLowerCase(Locale.ROOT));
                if (value.isEmpty()) {
                    error(source, entry, String.format("Failed to find value '%s' for property '%s' and block '%s'", split[1], split[0], key));
                    return null;
                }
                state = state.setValue(property, (Comparable) value.get());
            }
            return state;
        }

        final Matcher matcher = KEY_VALUE_REGEX.matcher(valueToParse);
        if (matcher.matches()) {
            final String domain = matcher.group(1);
            final String resource = matcher.group(2);
            final ResourceLocation key = toKey(domain, resource);

            if (key == null) {
                error(source, entry, "Invalid resource location '" + valueToParse + "'");
                return null;
            } else if (!isDomainValid(key.getNamespace())) {
                error(source, entry, "No matching mod domain found for '" + key + "'");
                return null;
            } else if (!isValidKey(key)) {
                error(source, entry, "No matching content found for '" + key + "'");
                return null;
            }

            return BuiltInRegistries.BLOCK.get(key).defaultBlockState();
        }
        return null;
    }

    public static class BlockOut extends BlockStateConfigList<BlockState> {

        public BlockOut(String name, Consumer<BlockStateConfigList<BlockState>> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected BlockState parseValue(String source, String entry, @Nullable String value) {
            return super.parseBlockState(source, entry, value);
        }
    }

    public static class BlockChanceOut extends BlockStateConfigList<BlockReplacementData> {

        public BlockChanceOut(String name, Consumer<BlockStateConfigList<BlockReplacementData>> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected BlockReplacementData parseValue(String source, String key, String value) {

            //minecraft:dirt=minecraft:stone
            //minecraft:dirt=minecraft:stone[axis:y]
            //minecraft:dirt=minecraft:stone;@chance(0.5);@nbt({"energy":0})
            //minecraft:dirt=~;@chance(0.5) keeps the existing block

            final BlockReplacementData blockReplacementData = new BlockReplacementData();

            // TODO refactor to be part of normal block out, then have this extend that adding chance check
            if(value.contains(";")) {
                final String[] entries = value.split(";");

                // First entry should always be block, ~ will skip replacement and use the existing block
                if(entries.length == 0 || !parseBlock(source, key, entries[0], blockReplacementData)) {
                    return null;
                }

                for(int i = 1; i < entries.length; i++) {
                    final String subEntry = entries[i];

                    //TODO add ability to selectively copy fields from source and target blockEntity
                    //TODO add ability to conditionally set data based on source field

                    final Matcher chanceMatcher = CHANCE_REGEX.matcher(subEntry);
                    if(chanceMatcher.matches()) {
                        blockReplacementData.setChance(Math.min(1, Math.max(0, Float.parseFloat(chanceMatcher.group(1)))));
                        continue;
                    }

                    final Matcher nbtMatcher = NBT_REGEX.matcher(subEntry);
                    if(nbtMatcher.matches()) {
                        //TODO regex detect if JSON is valid
                        try {
                            blockReplacementData.setBlockNBT(TagParser.parseTag(nbtMatcher.group(1)));
                        }
                        catch (CommandSyntaxException e) {
                            error(source, key, "Failed to read NBT json provided. Cause: " + e.getMessage());
                            return null;
                        }
                        continue;
                    }

                    // Log regex entries that can't be matched
                    error(source, key, "Unknown entry '" + subEntry + "' from value '" + value + "'");
                }

                return blockReplacementData;
            }
            // Legacy block state & chance pair TODO remove after 1.12
            // PORT: a bad chance threw out of common setup in 1.12, now it is a logged parse error. Values ending
            // in ']' are property states `mod:block[a:b,c:d]` and are not split on their ','
            else if (value.contains(",") && !value.endsWith("]")) {
                final int splitIndex = value.lastIndexOf(',');
                final String chanceStr = value.substring(splitIndex + 1);
                final float chance;
                try {
                    chance = Float.parseFloat(chanceStr);
                }
                catch (NumberFormatException e) {
                    error(source, key, "Invalid chance '" + chanceStr + "' from value '" + value + "'");
                    return null;
                }
                if(Float.isNaN(chance)) {
                    error(source, key, "Invalid chance '" + chanceStr + "' from value '" + value + "'");
                    return null;
                }
                if(!parseBlock(source, key, value.substring(0, splitIndex), blockReplacementData)) {
                    return null;
                }
                return blockReplacementData.setChance(Math.min(1, Math.max(0, chance)));
            }

            if(!parseBlock(source, key, value, blockReplacementData)) {
                return null;
            }
            return blockReplacementData;
        }

        /**
         * Parses the block part of a replacement value
         *
         * @return false if the block could not be parsed, ~ keeps the existing block and is valid
         */
        private boolean parseBlock(String source, String key, String blockStr, BlockReplacementData data) {
            if(blockStr.equals("~")) {
                return true;
            }
            // PORT: 1.12 kept entries whose output block failed to parse, silently turning them into 'keep existing block'
            final BlockState state = super.parseBlockState(source, key, blockStr);
            if(state == null) {
                error(source, key, "Failed to parse block '" + blockStr + "'");
                return false;
            }
            data.setBlock(state);
            return true;
        }
    }

    public static class FloatOut extends BlockStateConfigList<Float> {

        public FloatOut(String name, Consumer<BlockStateConfigList<Float>> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected Float parseValue(String source, String entry, String value) {
            try {
                return Float.parseFloat(value);
            }
            catch (NumberFormatException e) {
                error(source, entry, "Value is not a Float");
            }
            return null;
        }
    }
}
// PORTED
