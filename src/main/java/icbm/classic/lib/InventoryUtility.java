package icbm.classic.lib;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robin)
 */
public class InventoryUtility
{

    public static void dropInventory(World world, BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile != null)
        {
            tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent((handler) -> {
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
            });
        }
    }

    public static ItemEntity dropItemStack(World world, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount)
    {
        //TODO fire drop events if not already done by forge
        //TODO add banned item filtering, prevent creative mode only items from being dropped
        if (world != null && !world.isRemote && !itemStack.isEmpty())
        {
            double randomX = 0;
            double randomY = 0;
            double randomZ = 0;

            if (randomAmount > 0)
            {
                randomX = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomY = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomZ = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
            }

            ItemEntity entityitem = new ItemEntity(world, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0)
            {
                entityitem.setMotion(0, 0, 0);
            }

            if (itemStack.hasTag())
            {
                entityitem.getItem().setTag(itemStack.getTag().copy());
            }

            entityitem.setPickupDelay(delay);
            world.addEntity(entityitem);
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
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB) && stackA.getCount() == stackB.getCount();
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
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB);
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
        return doTagsMatch(stackA.getTag(), stackB.getTag());
    }

    public static boolean doTagsMatch(final CompoundNBT tag, final CompoundNBT tag2)
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
