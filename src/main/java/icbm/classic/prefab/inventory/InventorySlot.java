package icbm.classic.prefab.inventory;

import lombok.Getter;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;


public class InventorySlot {

    @Getter
    private final int slot;

    /**
     * Validator to check if an item is allowed in the slot
     */
    @Getter
    private final Function<ItemStack, Boolean> validator;

    /**
     * Callback for each tick on the slot
     */
    @Getter
    private Function<ItemStack, ItemStack> onTick;

    /**
     * Callback on slot contents change
     */
    @Getter
    private Consumer<ItemStack> onContentChanged;

    /**
     * Validator to see if the stack is allowed for insert.
     * Can be used for dynamically disable slot access.
     */
    @Getter
    private Function<ItemStack, Boolean> onInsertStack;

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

    public InventorySlot withInsertCheck(Function<ItemStack, Boolean> onInsertStack) {
        this.onInsertStack = onInsertStack;
        return this;
    }
}
