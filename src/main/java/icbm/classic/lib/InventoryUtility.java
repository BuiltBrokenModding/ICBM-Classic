package icbm.classic.lib;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robert)
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
    public static List<EntityItem> dropBlockAsItem(World world, BlockPos pos, boolean destroy)
    {
        List<EntityItem> entities = new ArrayList();
        if (!world.isRemote)
        {
            IBlockState state = world.getBlockState(pos);

            if (state != null && !state.getBlock().isAir(state, world, pos))
            {
                List<ItemStack> items = state.getBlock().getDrops(world, pos, state, 0);

                for (ItemStack itemStack : items)
                {
                    EntityItem entityItem = dropItemStack(world, new Pos(pos), itemStack, 10);
                    if (entityItem != null)
                    {
                        entities.add(entityItem);
                    }
                }
            }
            if (destroy)
            {
                world.setBlockToAir(pos);
            }
        }
        return entities;
    }

    public static EntityItem dropItemStack(World world, IPos3D position, ItemStack itemStack, int delay)
    {
        return dropItemStack(world, position, itemStack, delay, 0f);
    }

    public static EntityItem dropItemStack(World world, IPos3D position, ItemStack itemStack, int delay, float randomAmount)
    {
        return dropItemStack(world, position.x(), position.y(), position.z(), itemStack, delay, randomAmount);
    }

    public static void dropInventory(World world, BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            final IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(handler != null) {

                final double x = pos.getX() + 0.5;
                final double y = pos.getY() + 0.5;
                final double z = pos.getZ() + 0.5;

                for(int slot = 0; slot < handler.getSlots(); slot++) {
                    final ItemStack stack = handler.getStackInSlot(0);
                    if(handler instanceof IItemHandlerModifiable) {
                        ((IItemHandlerModifiable) handler).setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    dropItemStack(world, x, y, z, stack, 0, 0);
                }
            }
        }
    }

    public static EntityItem dropItemStack(World world, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount)
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

            EntityItem entityitem = new EntityItem(world, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0)
            {
                entityitem.motionX = 0;
                entityitem.motionY = 0;
                entityitem.motionZ = 0;
            }

            if (itemStack.hasTagCompound())
            {
                entityitem.getItem().setTagCompound(itemStack.getTagCompound().copy());
            }

            entityitem.setPickupDelay(delay);
            world.spawnEntity(entityitem);
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
        return doTagsMatch(stackA.getTagCompound(), stackB.getTagCompound());
    }

    public static boolean doTagsMatch(final NBTTagCompound tag, final NBTTagCompound tag2)
    {
        boolean firstTagEmpty = tag == null || tag.hasNoTags();
        boolean firstTagEmpty2 = tag2 == null || tag2.hasNoTags();
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
