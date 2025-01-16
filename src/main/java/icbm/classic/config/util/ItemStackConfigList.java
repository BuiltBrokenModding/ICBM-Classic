package icbm.classic.config.util;


import icbm.classic.lib.ForgeRegistryHelpers;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Documentation <a href="https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-itemstack">ItemStack Config</a>
 * @param <VALUE> to use
 */
public abstract class ItemStackConfigList<VALUE> extends ResourceConfigList<ItemStackConfigList, ItemStack, VALUE> {
    public ItemStackConfigList(String name, Consumer<ItemStackConfigList> reloadCallback) {
        super(name, "https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-itemstack", reloadCallback);
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
        final ResourceLocation key = content.getItem().getRegistryName();
        defaultMatchers
            .computeIfAbsent(key, k -> new ArrayList<>())
            .add(new ResourceConfigEntry<>("default_metadata", order, (itemStack) -> ItemStack.areItemsEqual(itemStack, content) ? value : null));
    }

    //TODO add support for NBT

    @Override
    protected boolean isValidKey(ResourceLocation targetKey) {
        return ForgeRegistryHelpers.contains(ForgeRegistries.ITEMS, targetKey);
    }

    @Override
    protected ResourceLocation getContentKey(ItemStack itemStack) {
        return itemStack.getItem().getRegistryName();
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
