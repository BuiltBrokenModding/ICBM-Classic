package icbm.classic.prefab.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/29/2018.
 */
public class ExternalWrapper implements IItemHandlerModifiable
{
    public final ExternalInventory inventory;

    public ExternalWrapper(ExternalInventory inventory)
    {
        this.inventory = inventory;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlots()
    {
        return inventory.getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return inventory.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (!stack.isEmpty() && slot >= 0 && slot < inventory.getSizeInventory() && inventory.canInsertItem(slot, stack, null))
        {
            ItemStack slotStack = getStackInSlot(slot);
            if (slotStack.isEmpty())
            {
                int cap = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());
                int insert = Math.min(cap, stack.getCount());
                ItemStack re = stack.copy();
                re.setCount(stack.getCount() - insert);
                if (re.getCount() <= 0)
                {
                    re = ItemStack.EMPTY;
                }

                if (!simulate)
                {
                    ItemStack insertStack = stack.copy();
                    insertStack.setCount(insert);
                    inventory.setInventorySlotContents(slot, insertStack);
                }

                return re;
            }
            else if (InventoryUtility.stacksMatch(slotStack, stack))
            {
                int cap = Math.min(slotStack.getMaxStackSize(), inventory.getInventoryStackLimit());
                int room = Math.max(0, cap - slotStack.getCount());
                int take = Math.min(room, stack.getCount());
                if (room > 0 && take > 0)
                {
                    ItemStack re = stack.copy();
                    re.setCount(stack.getCount() - take);
                    if (re.getCount() <= 0)
                    {
                        re = ItemStack.EMPTY;
                    }

                    if (!simulate)
                    {
                        slotStack.setCount(slotStack.getCount() + take);
                        inventory.setInventorySlotContents(slot, slotStack);
                    }

                    return re;
                }
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        ItemStack slotStack = getStackInSlot(slot);
        if (!slotStack.isEmpty() && inventory.canExtractItem(slot, slotStack, null))
        {
            ItemStack copy = slotStack.copy();
            if (copy.getCount() >= amount)
            {
                if (!simulate)
                {
                    inventory.setInventorySlotContents(0, ItemStack.EMPTY);
                }
                return copy;
            }
            else
            {
                copy.setCount(amount);
                slotStack.setCount(slotStack.getCount() - amount);
                if (!simulate)
                {
                    inventory.setInventorySlotContents(0, slotStack);
                }
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return inventory.getInventoryStackLimit();
    }
}
