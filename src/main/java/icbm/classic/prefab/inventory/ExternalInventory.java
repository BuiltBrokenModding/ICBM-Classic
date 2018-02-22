package icbm.classic.prefab.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * Version of the basic ISidedInventory that is designed to be used as a replacement for
 * the conventional inventory used in machines.
 *
 * @author Darkguardsman
 */
public class ExternalInventory extends BasicInventory implements IExternalInventory, ISidedInventory, IItemHandlerModifiable
{
    /**
     * Access able slots side all
     */
    protected int[] openSlots;
    /**
     * Host tileEntity
     */
    protected IInventoryProvider host;

    public ExternalInventory(IInventoryProvider inv, int slots)
    {
        super(slots);
        this.host = inv;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return i < this.getSizeInventory();
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing facing)
    {
        if (openSlots == null || openSlots.length != this.getSizeInventory())
        {
            this.openSlots = new int[this.getSizeInventory()];
            for (int i = 0; i < this.openSlots.length; i++)
            {
                openSlots[i] = i;
            }
        }
        return this.openSlots;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing side)
    {
        return this.isItemValidForSlot(i, itemstack) && host.canStore(itemstack, i, side);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing side)
    {
        return host.canRemove(itemstack, i, side);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {
        if (host instanceof TileEntity)
        {
            ((TileEntity) host).markDirty();
        }
    }

    @Override
    protected void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        host.onInventoryChanged(slot, prev, item);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    @Override
    public void clear()
    {
        this.inventoryMap.clear();
    }


    //===========================================================
    //===============  IItemHandlerModifiable   =================
    //===========================================================

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlots()
    {
        return getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (!stack.isEmpty() && slot >= 0 && slot < getSizeInventory())
        {
            ItemStack slotStack = getStackInSlot(slot);
            if (slotStack.isEmpty())
            {
                int cap = Math.min(stack.getMaxStackSize(), getInventoryStackLimit());
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
                    setInventorySlotContents(slot, insertStack);
                }

                return re;
            }
            else if (InventoryUtility.stacksMatch(slotStack, stack))
            {
                int cap = Math.min(slotStack.getMaxStackSize(), getInventoryStackLimit());
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
                        setInventorySlotContents(slot, slotStack);
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
        if (!slotStack.isEmpty())
        {
            ItemStack copy = slotStack.copy();
            if (copy.getCount() >= amount)
            {
                if (!simulate)
                {
                    setInventorySlotContents(0, ItemStack.EMPTY);
                }
                return copy;
            }
            else
            {
                copy.setCount(amount);
                slotStack.setCount(slotStack.getCount() - amount);
                if (!simulate)
                {
                    setInventorySlotContents(0, slotStack);
                }
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return getInventoryStackLimit();
    }
}
