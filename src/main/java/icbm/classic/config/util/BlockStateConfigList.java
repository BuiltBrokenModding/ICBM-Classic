package icbm.classic.config.util;

import com.google.common.collect.ImmutableMap;
import icbm.classic.lib.ForgeRegistryHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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
public abstract class BlockStateConfigList<VALUE> extends ResourceConfigList<BlockStateConfigList<VALUE>, IBlockState, VALUE> {

    protected static final Pattern BLOCK_PROP_REGEX = Pattern.compile("^(.*):([^=\\s]*)\\[((?:[\\w.]+:[\\w.]+,?)+)\\](?:=(.*))?");
    protected static final Pattern CHANCE_REGEX = Pattern.compile("^@chance\\(([0-9]*[.][0-9]*)\\)$");
    protected static final Pattern NBT_REGEX = Pattern.compile("^@nbt\\((.*)\\)$");


    public BlockStateConfigList(String name, Consumer<BlockStateConfigList<VALUE>> reloadCallback) {
        super(name, "https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states", reloadCallback);
        addMatcher(BLOCK_PROP_REGEX, this::handleBlockProps);
        addMatcher(META_KEY_REGEX, this::handleMeta);
        addMatcher(KEY_VALUE_REGEX, this::handleSimple);
    }

    @Override
    protected ResourceLocation getContentKey(IBlockState iBlockState) {
        return iBlockState.getBlock().getRegistryName();
    }

    @Override
    protected Function<IBlockState, VALUE> getMetaValue(ResourceLocation key, int metadata, @Nullable VALUE value) {
        return (blockState) -> {
            if (getContentKey(blockState) == key && blockState.getBlock().getMetaFromState(blockState) == metadata) {
                return value;
            }
            return null;
        };
    }

    protected Function<IBlockState, VALUE> getPropValue(ResourceLocation key, Map<IProperty, Function<Comparable, Boolean>> propMatchers, @Nullable VALUE value) {
        return (blockState) -> {
            if (getContentKey(blockState) == key) {
                final ImmutableMap<IProperty<?>, Comparable<?>> stateProps = blockState.getProperties();
                for(IProperty propKey: propMatchers.keySet()) {
                    if(!stateProps.containsKey(propKey)) {
                        return null;
                    }

                    final Function<Comparable, Boolean> check = propMatchers.get(propKey);
                    if(check != null && !check.apply(stateProps.get(propKey))) {
                        return null;
                    }
                }
                return value;
            }
            return null;
        };
    }

    protected ResourceConfigEntry<IBlockState, VALUE> handleBlockProps(Matcher domainMatcher, String source, String entry, int index) {
        final String domain = domainMatcher.group(1);
        final String resource = domainMatcher.group(2);
        final ResourceLocation key = new ResourceLocation(domain, resource);

        if (!isDomainValid(key.getResourceDomain())) {
            error(source, entry, "No matching mod domain found for '" + key + "'");
            return null;
        } else if (!isValidKey(key)) {
            error(source, entry, "No matching content found for '" + key + "'");
            return null;
        }

        final Block block = ForgeRegistries.BLOCKS.getValue(key);
        final String[] props = domainMatcher.group(3).split(",");
        final Map<IProperty, Function<Comparable, Boolean>> propMatchers = collectBlockProps(source, entry, block, props);
        if (propMatchers == null) {
            return null;
        }

        final String valueStr = domainMatcher.group(4);
        final VALUE value = parseValue(source, entry, valueStr);

        final Function<IBlockState, VALUE> matcher = getPropValue(key, propMatchers, value);
        return new ResourceConfigEntry<>("resource_block_state", index, matcher);
    }

    @Override
    protected boolean isValidKey(ResourceLocation targetKey) {
        return ForgeRegistryHelpers.contains(ForgeRegistries.BLOCKS, targetKey);
    }

    protected Map<IProperty, Function<Comparable, Boolean>> collectBlockProps(String source, String entry, Block block, String[] props) {
        final Map<IProperty, Function<Comparable, Boolean>> matchers = new HashMap();
        for (String propEntry : props) {
            final Matcher propMatcher = KEY_VALUE_REGEX.matcher(propEntry);
            if (!propMatcher.matches()) {
                // TODO error log
                return null;
            }
            final String propKey = propMatcher.group(1);
            final String propValue = propMatcher.group(2);
            //TODO consider using group 3 as a boolean contains check

            final IProperty property = block.getBlockState().getProperty(propKey);
            if (property == null) {
                error(source, entry, String.format("Failed to find property '%s' for block '%s' matching entry `%s`", propKey, block.getRegistryName(), propEntry));
                return null;
            }

            if (propValue.equals("~")) {
                matchers.put(property, (o) -> true);
            } else if (propValue.startsWith("~") || propValue.endsWith("~")) {
                final String stringMatch = propValue.substring(1).trim();
                final List<Comparable<?>> valuesToMatch = (List<Comparable<?>>) property.getAllowedValues().stream()
                    .filter(o -> {
                        if (propValue.endsWith("~")) {
                            return property.getName((Comparable) o).endsWith(stringMatch);
                        }
                        return property.getName((Comparable) o).startsWith(stringMatch);
                    }).collect(Collectors.toList());
                if (valuesToMatch.isEmpty()) {
                    error(source, entry, String.format("Failed to find values matching '%s' for property '%s' and block '%s' matching entry '%s'", propValue, propKey, block.getRegistryName(), propEntry));
                    return null;
                }
                matchers.put(property, valuesToMatch::contains);
            } else {
                // Simple value matcher
                final Optional value = property.getAllowedValues().stream().filter(o -> property.getName((Comparable) o).equalsIgnoreCase(propValue)).findFirst();
                if (!value.isPresent()) {
                    error(source, entry, String.format("Failed to find values matching '%s' for property '%s' and block '%s' matching entry '%s'", propValue, propKey, block.getRegistryName(), propEntry));
                    return null;
                }
                matchers.put(property, (o) -> Objects.equals(value.get(), o));
            }
        }
        return matchers;
    }


    protected IBlockState parseBlockState(String source, String entry, String valueToParse) {
        final Matcher metaMatcher = META_KEY_REGEX.matcher(valueToParse);
        if (metaMatcher.matches()) {
            final String domain = metaMatcher.group(1);
            final String resource = metaMatcher.group(2);
            final ResourceLocation key = new ResourceLocation(domain, resource);
            if (!isDomainValid(key.getResourceDomain())) {
                error(source, entry, "No matching mod domain found for '" + key + "'");
                return null;
            } else if (!isValidKey(key)) {
                error(source, entry, "No matching content found for '" + key + "'");
                return null;
            }

            final int meta = Integer.parseInt(metaMatcher.group(3));

            final Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block != null) {
                return block.getStateFromMeta(meta);
            }
        }

        final Matcher matcher = KEY_VALUE_REGEX.matcher(valueToParse);
        if (matcher.matches()) {
            final String domain = matcher.group(1);
            final String resource = matcher.group(2);
            final ResourceLocation key = new ResourceLocation(domain, resource);

            if (!isDomainValid(key.getResourceDomain())) {
                error(source, entry, "No matching mod domain found for '" + key + "'");
                return null;
            } else if (!isValidKey(key)) {
                error(source, entry, "No matching content found for '" + key + "'");
                return null;
            }

            final Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block != null) {
                return block.getDefaultState();
            }
        }
        return null;
    }

    public static class BlockOut extends BlockStateConfigList<IBlockState> {

        public BlockOut(String name, Consumer<BlockStateConfigList<IBlockState>> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected IBlockState parseValue(String source, String entry, @Nullable String value) {
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
            //minecraft:dirt=minecraft:stone@2
            //minecraft:dirt=minecraft:stone[variant=stone]
            //minecraft:dirt=minecraft:stone;@chance(0.5);@nbt({"energy":0})

            final BlockReplacementData blockReplacementData = new BlockReplacementData();

            // TODO refactor to be part of normal block out, then have this extend that adding chance check
            if(value.contains(";")) {
                final String[] entries = value.split(";");

                // First entry should always be block state
                blockReplacementData.setBlockState(super.parseBlockState(source, key, entries[0]));

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
                            blockReplacementData.setBlockNBT(JsonToNBT.getTagFromJson(nbtMatcher.group(1)));
                        }
                        catch (NBTException e) {
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
            else if (value.contains(",")) {
                final String[] split = value.split(",");
                return blockReplacementData
                    .setChance(Math.min(1, Math.max(0, Float.parseFloat(split[1]))))
                    .setBlockState(super.parseBlockState(source, key, split[0]));
            }

            return blockReplacementData.setBlockState(super.parseBlockState(source, key, value));
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
