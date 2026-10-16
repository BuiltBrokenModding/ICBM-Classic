package icbm.classic.lib;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robin)
 */
public class InventoryUtility
{
    /**
     * Attempts to drop the block at the location as an item. Does not check what the block is
     * and can fail if the block doesn't contain items.
     *
     * @param world
     * @param pos
     * @param destroy - will break the block
     */
    public static List<ItemEntity> dropBlockAsItem(Level world, BlockPos pos, boolean destroy)
    {
        List<ItemEntity> entities = new ArrayList();
        if (!world.isClientSide())
        {
            BlockState state = world.getBlockState(pos);

            if (state != null && !state.isAir() && world instanceof ServerLevel serverLevel)
            {
                // PORT: 1.12 getDrops(world, pos, state, fortune=0) -> loot table drops without tool
                List<ItemStack> items = Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos));

                for (ItemStack itemStack : items)
                {
                    ItemEntity entityItem = dropItemStack(world, new Pos(pos), itemStack, 10);
                    if (entityItem != null)
                    {
                        entities.add(entityItem);
                    }
                }
            }
            if (destroy)
            {
                world.removeBlock(pos, false);
            }
        }
        return entities;
    }

    public static ItemEntity dropItemStack(Level world, IPos3D position, ItemStack itemStack, int delay)
    {
        return dropItemStack(world, position, itemStack, delay, 0f);
    }

    public static ItemEntity dropItemStack(Level world, IPos3D position, ItemStack itemStack, int delay, float randomAmount)
    {
        return dropItemStack(world, position.x(), position.y(), position.z(), itemStack, delay, randomAmount);
    }

    public static void dropInventory(Level world, BlockPos pos) {
        dropInventory(world, pos, null);
    }

    /**
     * Drops the contents of the block entity's item handler at the position.
     *
     * @param world    containing the block
     * @param pos      of the block
     * @param oldState state of the block being removed, null to use the block entity's cached state
     */
    public static void dropInventory(Level world, BlockPos pos, @Nullable BlockState oldState) {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null)
        {
            // PORT: Block#onRemove runs after the chunk already holds the new state (air), so a lookup through
            //  Level#getCapability(cap, pos, ctx) would read air and find no provider. Ask the still present
            //  block entity directly like 1.12 did with TileEntity#getCapability.
            final IItemHandler handler;
            if (tile instanceof ICapabilityHolderTile holder)
            {
                handler = holder.getCapability(Capabilities.ItemHandler.BLOCK, null);
            }
            else
            {
                final BlockState state = oldState != null ? oldState : tile.getBlockState();
                handler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, tile, null);
            }
            if(handler != null) {

                final double x = pos.getX() + 0.5;
                final double y = pos.getY() + 0.5;
                final double z = pos.getZ() + 0.5;

                for(int slot = 0; slot < handler.getSlots(); slot++) {
                    final ItemStack stack = handler.getStackInSlot(slot);
                    if(handler instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable) handler).setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    dropItemStack(world, x, y, z, stack, 0, 0);
                }
            }
        }
    }

    public static ItemEntity dropItemStack(Level world, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount)
    {
        //TODO fire drop events if not already done by forge
        //TODO add banned item filtering, prevent creative mode only items from being dropped
        if (world != null && !world.isClientSide() && !itemStack.isEmpty())
        {
            double randomX = 0;
            double randomY = 0;
            double randomZ = 0;

            if (randomAmount > 0)
            {
                randomX = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomY = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomZ = world.random.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
            }

            ItemEntity entityitem = new ItemEntity(world, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0)
            {
                entityitem.setDeltaMovement(Vec3.ZERO);
            }

            // PORT: 1.12 copied the stack NBT onto the entity's stack here, 1.21 components already travel with the stack

            entityitem.setPickUpDelay(delay);
            world.addFreshEntity(entityitem);
            return entityitem;
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
    public static boolean stacksMatchExact(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            return ItemStack.isSameItem(stackA, stackB) && doesStackNBTMatch(stackA, stackB) && stackA.getCount() == stackB.getCount();
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
    public static boolean stacksMatch(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            return ItemStack.isSameItem(stackA, stackB) && doesStackNBTMatch(stackA, stackB);
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
    public static boolean doesStackNBTMatch(ItemStack stackA, ItemStack stackB)
    {
        // PORT: stack NBT is now data components, compare the changes applied on top of the item defaults
        return Objects.equals(stackA.getComponentsPatch(), stackB.getComponentsPatch());
    }

    public static boolean doTagsMatch(final CompoundTag tag, final CompoundTag tag2)
    {
        boolean firstTagEmpty = tag == null || tag.isEmpty();
        boolean firstTagEmpty2 = tag2 == null || tag2.isEmpty();
        if (firstTagEmpty && firstTagEmpty2)
        {
            return true;
        }
        else if (!firstTagEmpty && firstTagEmpty2)
        {
            return false;
        }
        else if (firstTagEmpty && !firstTagEmpty2)
        {
            return false;
        }
        return tag.equals(tag2);
    }
}
// PORTED
