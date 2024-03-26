package icbm.classic.world.block.launcher.network;

import icbm.classic.api.ICBMClassicAPI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.items.CapabilityItemHandler;
import net.neoforged.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class NetworkInventory implements IItemHandler {

    private final LauncherNetwork network;
    private final List<SlotHolder> slots = new ArrayList();


    public void buildInventory() {
        slots.clear();

        network.getComponents().stream().filter(LauncherNode::isAcceptsItems).forEach((node) -> {
            final IItemHandler handler = getHandler(node.getSelf());
            if (handler != null) {
                final int slotCount = handler.getSlots();
                for (int i = 0; i < slotCount; i++) {
                    slots.add(new SlotHolder(handler, i));
                }
            }
        });
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    private IItemHandler getHandler(BlockEntity blockEntity) {
        if (tile.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, null) && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            final IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {

        validateSlotIndex(slot);

        final SlotHolder holder = slots.get(slot);
        return holder.handler.getStackInSlot(holder.getSlotIndex());
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

        validateSlotIndex(slot);

        final SlotHolder holder = slots.get(slot);
        return holder.handler.insertItem(holder.getSlotIndex(), stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

        validateSlotIndex(slot);

        final SlotHolder holder = slots.get(slot);
        return holder.handler.extractItem(holder.getSlotIndex(), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {

        validateSlotIndex(slot);

        final SlotHolder holder = slots.get(slot);
        return holder.handler.getSlotLimit(holder.getSlotIndex());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);

        final SlotHolder holder = slots.get(slot);
        return holder.handler.isItemValid(holder.getSlotIndex(), stack);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= slots.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");
    }

    @Data
    @AllArgsConstructor
    private static class SlotHolder {
        private final IItemHandler handler;
        private final int slotIndex;
    }
}
