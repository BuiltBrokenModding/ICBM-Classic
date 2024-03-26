package icbm.classic.prefab.inventory;

import icbm.classic.lib.tile.ITick;
import lombok.Getter;
import net.minecraft.util.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.BiConsumer;

public class InventoryWithSlots extends ItemStackHandler implements ITick {

    private final InventorySlot[] slotHandlers;

    @Getter
    private BiConsumer<Integer, ItemStack> onContentChange;

    public InventoryWithSlots(int size) {
        super(size);
        slotHandlers = new InventorySlot[size];
    }

    @Override
    public void setSize(int size) {
        stacks = NonNullList.withSize(Math.max(size, stacks.size()), ItemStack.EMPTY);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (onContentChange != null) {
            onContentChange.accept(slot, getStackInSlot(slot));
        }
        getSlot(slot).map(InventorySlot::getOnContentChanged).ifPresent(s -> s.accept(getStackInSlot(slot)));
    }

    public InventoryWithSlots withSlot(InventorySlot slot) {
        slotHandlers[slot.getSlot()] = slot;
        return this;
    }

    public InventoryWithSlots withChangeCallback(BiConsumer<Integer, ItemStack> onChange) {
        this.onContentChange = onChange;
        return this;
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) && getSlot(slot).map(InventorySlot::getOnInsertStack).map(s -> s.apply(stack)).orElse(true) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);
        return getSlot(slot).map(InventorySlot::getValidator).map(s -> s.apply(stack)).orElse(false);
    }

    public Optional<InventorySlot> getSlot(int slot) {
        if (slot < slotHandlers.length) {
            return Optional.ofNullable(slotHandlers[slot]);
        }
        return Optional.empty();
    }

    @Override
    public void update(int tick, boolean isServer) {
        if (isServer) {
            for (InventorySlot slot : slotHandlers) {
                if (slot != null && slot.getOnTick() != null) {
                    setStackInSlot(slot.getSlot(), slot.getOnTick().apply(getStackInSlot(slot.getSlot())));
                }
            }
        }
    }
}
