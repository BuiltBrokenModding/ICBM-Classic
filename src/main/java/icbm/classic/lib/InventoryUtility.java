package icbm.classic.lib;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robert)
 */
public class InventoryUtility {
    /**
     * Attempts to drop the block at the location as an item. Does not check what the block is
     * and can fail if the block doesn't contain items.
     *
     * @param level
     * @param pos
     * @param destroy - will break the block
     */
    public static List<ItemEntity> dropBlockAsItem(ServerLevel level, BlockPos pos, boolean destroy) {
        List<ItemEntity> entities = new ArrayList();
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (!state.getBlock().isEmpty(state)) {
                List<ItemStack> items = state.getBlock().getDrops(state, level, pos, blockEntity);

                for (ItemStack itemStack : items) {
                    ItemEntity entityItem = dropItemStack(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), itemStack, 10);
                    if (entityItem != null) {
                        entities.add(entityItem);
                    }
                }
            }
            if (destroy) {
                level.setBlock(pos, state, 1);
            }
        }
        return entities;
    }

    public static ItemEntity dropItemStack(Level level, Vec3 position, ItemStack itemStack, int delay) {
        return dropItemStack(level, position, itemStack, delay, 0f);
    }

    public static ItemEntity dropItemStack(Level level, Vec3 position, ItemStack itemStack, int delay, float randomAmount) {
        return dropItemStack(level, position.x(), position.y(), position.z(), itemStack, delay, randomAmount);
    }

    public static void dropInventory(Level level, BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && blockEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            final IItemHandler handler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {

                final double x = pos.getX() + 0.5;
                final double y = pos.getY() + 0.5;
                final double z = pos.getZ() + 0.5;

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    final ItemStack stack = handler.getStackInSlot(slot);
                    if (handler instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable) handler).setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    dropItemStack(level, x, y, z, stack, 0, 0);
                }
            }
        }
    }

    public static ItemEntity dropItemStack(Level level, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount) {
        //TODO fire drop events if not already done by forge
        //TODO add banned item filtering, prevent creative mode only items from being dropped
        if (level != null && !level.isClientSide() && !itemStack.isEmpty()) {
            double randomX = 0;
            double randomY = 0;
            double randomZ = 0;

            if (randomAmount > 0) {
                randomX = level.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomY = level.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomZ = level.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
            }

            ItemEntity itemEntity = new ItemEntity(level, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0) {
                itemEntity.motionX = 0;
                itemEntity.motionY = 0;
                itemEntity.motionZ = 0;
            }

            if (itemStack.hasTag()) {
                itemEntity.getItem().setTag(itemStack.getTag().copy());
            }

            itemEntity.setPickUpDelay(delay);
            level.addFreshEntity(itemEntity);
            return itemEntity;
        }
        return null;
    }

    /**
     * Checks if the two item stacks match each other exactly. Item, meta, stacksize, nbt
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatchExact(ItemStack stackA, ItemStack stackB) {
        if (!stackA.isEmpty() && !stackB.isEmpty()) {
            return ItemStack.matches(stackA, stackB);
        }
        return stackA.isEmpty() && stackB.isEmpty();
    }

    /**
     * Checks if two item stacks match each other using item, meta, and nbt to compare
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatch(ItemStack stackA, ItemStack stackB) {
        if (!stackA.isEmpty() && !stackB.isEmpty()) {
            return ItemStack.isSameItemSameTags(stackB, stackB);
        }
        return stackA.isEmpty() && stackB.isEmpty();
    }


    /**
     * Checks if two itemStack's nbt matches exactly. Does not check item, stacksize, or damage value.
     *
     * @param stackA - item stack a, can't be null
     * @param stackB - item stack a, can't be null
     * @return true if the stack's nbt matches
     */
    public static boolean doesStackNBTMatch(ItemStack stackA, ItemStack stackB) {
        return Objects.equals(stackA.getTag(), stackB.getTag()) && stackA.areAttachmentsCompatible(stackB);
    }
}
