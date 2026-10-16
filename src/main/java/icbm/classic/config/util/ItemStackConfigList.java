package icbm.classic.config.util;



import icbm.classic.lib.explosive.ExplosiveStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Documentation <a href="https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-itemstack">ItemStack Config</a>
 * @param <VALUE> to use
 */
public abstract class ItemStackConfigList<VALUE> extends ResourceConfigList<ItemStackConfigList, ItemStack, VALUE> {
    public ItemStackConfigList(String name, Consumer<ItemStackConfigList> reloadCallback) {
        super(name, "https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-itemstack", reloadCallback);
        addMatcher(META_KEY_REGEX, this::handleMeta);
        addMatcher(KEY_VALUE_REGEX, this::handleSimple);
    }

    @Override
    public VALUE getValue(ItemStack state) {
        if (state == null || state.isEmpty()) {
            return null;
        }
        //TODO find a way to cache ItemStack for faster performance
        return super.getValue(state);
    }

    public void setDefaultMeta(ItemStack content, VALUE value, int order) {
        final Item item = content.getItem();
        final ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        // PORT: 1.12 compared item + metadata (ItemStack.areItemsEqual). Metadata now maps to the explosive
        // component and the damage value, other components (custom name, customizations in custom data) are ignored.
        final ResourceLocation explosiveId = ExplosiveStacks.getExplosiveName(content);
        final int damage = content.getDamageValue();
        defaultMatchers
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(new ResourceConfigEntry<>("default_metadata", order, (itemStack) ->
                itemStack.is(item)
                    && itemStack.getDamageValue() == damage
                    && Objects.equals(explosiveId, ExplosiveStacks.getExplosiveName(itemStack)) ? value : null));
    }

    //TODO add support for NBT

    @Override
    protected Function<ItemStack, VALUE> getMetaValue(ResourceLocation key, int metadata, @Nullable VALUE value) {
        // TODO check that the item supports sub-types
        // PORT: metadata is gone, the closest 1.21 value is the damage of damageable items
        return (stack) -> {
            if(key.equals(getContentKey(stack)) && stack.getDamageValue() == metadata) {
                return value;
            }
            return null;
        };
    }

    @Override
    protected boolean isValidKey(ResourceLocation targetKey) {
        return BuiltInRegistries.ITEM.containsKey(targetKey);
    }

    @Override
    protected ResourceLocation getContentKey(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem());
    }

    public static class ContainsCheck extends ItemStackConfigList<Boolean> {

        public ContainsCheck(String name, Consumer<ItemStackConfigList> reloadCallback) {
            super(name, reloadCallback);
        }

        public boolean isContained(ItemStack stack) {
            Boolean value = super.getValue(stack);
            return value != null && value;
        }

        public boolean isAllowed(ItemStack stack, boolean ban) {
            return ban && !isContained(stack) || !ban && isContained(stack);
        }

        @Override
        protected Function<ItemStack, Boolean> getDomainValue(String domain, Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getDomainValue(domain, true);
        }

        @Override
        protected Function<ItemStack, Boolean> getSimpleValue(ResourceLocation targetKey, Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getSimpleValue(targetKey, true);
        }

        @Override
        protected Function<ItemStack, Boolean> getFuzzyValue(String domain, String path, Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getFuzzyValue(domain, path, true);
        }

        @Override
        protected Function<ItemStack, Boolean> getMetaValue(ResourceLocation key, int metadata, @Nullable Boolean disable) {
            if(Boolean.TRUE.equals(disable)) {
                return null;
            }
            return super.getMetaValue(key, metadata, true);
        }

        @Override
        protected Boolean parseValue(String source, String entry, String value) {
            return Boolean.parseBoolean(value);
        }
    }

    public static class IntOut extends ItemStackConfigList<Integer> {

        public IntOut(String name, Consumer<ItemStackConfigList> reloadCallback) {
            super(name, reloadCallback);
        }

        @Override
        protected Integer parseValue(String source, String entry, String value) {
            try {
                return Integer.parseInt(value, 10);
            }
            catch (NumberFormatException e) {
                error(source, entry, "Value is not an integer");
            }
            return null;
        }
    }

    public static class FloatOut extends ItemStackConfigList<Float> {

        public FloatOut(String name, Consumer<ItemStackConfigList> reloadCallback) {
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
