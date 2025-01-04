package icbm.classic.config.util;

import com.google.common.collect.ImmutableMap;
import icbm.classic.lib.ForgeRegistryHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

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

    protected static final Pattern BLOCK_PROP_REGEX = Pattern.compile("^(.*):([^=\\s]*)\\[((?:[\\w.]+:[\\w.]+,?)+)\\](?:=(.*))?");


    public BlockStateConfigList(String name, Consumer<BlockStateConfigList<VALUE>> reloadCallback) {
        super(name, "https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states", reloadCallback);
        addMatcher(BLOCK_PROP_REGEX, this::handleBlockProps);
        addMatcher(KEY_VALUE_REGEX, this::handleSimple);
    }

    @Override
    protected ResourceLocation getContentKey(BlockState iBlockState) {
        return iBlockState.getBlock().getRegistryName();
    }

    protected Function<BlockState, VALUE> getPropValue(ResourceLocation key, Map<IProperty, Function<Comparable, Boolean>> propMatchers, @Nullable VALUE value) {
        return (blockState) -> {
            if (getContentKey(blockState) == key) {
                final Collection<IProperty<?>> stateProps = blockState.getProperties();
                for(IProperty propKey: propMatchers.keySet()) {
                    if(!stateProps.contains(propKey)) {
                        return null;
                    }

                    final Function<Comparable, Boolean> check = propMatchers.get(propKey);
                    if(check != null && !check.apply(blockState.get(propKey))) {
                        return null;
                    }
                }
                return value;
            }
            return null;
        };
    }

    protected ResourceConfigEntry<BlockState, VALUE> handleBlockProps(Matcher domainMatcher, String source, String entry, int index) {
        final String domain = domainMatcher.group(1);
        final String resource = domainMatcher.group(2);
        final ResourceLocation key = new ResourceLocation(domain, resource);

        if (!isDomainValid(key.getNamespace())) {
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

        final Function<BlockState, VALUE> matcher = getPropValue(key, propMatchers, value);
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

            final IProperty property = block.getDefaultState().getProperties().stream().filter(pr -> pr.getName().equalsIgnoreCase(propKey)).findFirst().orElse(null);
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


    protected BlockState parseBlockState(String source, String entry, String valueToParse) {
        final Matcher matcher = KEY_VALUE_REGEX.matcher(valueToParse);
        if (matcher.matches()) {
            final String domain = matcher.group(1);
            final String resource = matcher.group(2);
            final ResourceLocation key = new ResourceLocation(domain, resource);

            if (!isDomainValid(key.getNamespace())) {
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


    public static class ContainsCheck extends BlockStateConfigList<Boolean> {

        public ContainsCheck(String name, Consumer<BlockStateConfigList<Boolean>> reloadCallback) {
            super(name, reloadCallback);
        }

        public boolean isAllowed(BlockState stack) {
            Boolean value = super.getValue(stack);
            return value == null || value;
        }

        @Override
        protected Function<BlockState, Boolean> getDomainValue(String domain, Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getDomainValue(domain, true);
        }

        @Override
        protected Function<BlockState, Boolean> getSimpleValue(ResourceLocation targetKey, Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getSimpleValue(targetKey, true);
        }

        @Override
        protected Boolean parseValue(String source, String entry, String value) {
            return Boolean.parseBoolean(value);
        }
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

    public static class BlockChanceOut extends BlockStateConfigList<Pair<BlockState, Float>> {

        public BlockChanceOut(String name, Consumer<BlockStateConfigList<Pair<BlockState, Float>>> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected Pair<BlockState, Float> parseValue(String source, String entry, String value) {

            if (value.contains(",")) { //TODO use regex to ensure we only have 1 comma
                final String[] split = value.split(",");
                return Pair.of(super.parseBlockState(source, entry, split[0]), Math.min(1, Math.max(0, Float.parseFloat(split[1]))));
            }
            return Pair.of(super.parseBlockState(source, entry, value), null);
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
