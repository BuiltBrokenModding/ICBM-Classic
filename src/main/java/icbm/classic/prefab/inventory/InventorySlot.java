package icbm.classic.prefab.inventory;

import lombok.Getter;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;


public class InventorySlot {

    @Getter
    private final int slot;

    @Getter
    private final Function<ItemStack, Boolean> validator;

    @Getter
    private Function<ItemStack, ItemStack> onTick;

    @Getter
    private Consumer<ItemStack> onContentChanged;

    public InventorySlot(int slot, Function<ItemStack, Boolean> validator) {
        this.slot = slot;
        this.validator = validator;
    }

    public InventorySlot withTick(Function<ItemStack, ItemStack> onTick) {
        this.onTick = onTick;
        return this;
    }

    public InventorySlot withChangeCallback(Consumer<ItemStack> onChange) {
        this.onContentChanged = onChange;
        return this;
    }
}
