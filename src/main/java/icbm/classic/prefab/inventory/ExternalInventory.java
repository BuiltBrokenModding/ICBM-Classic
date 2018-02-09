package icbm.classic.prefab.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * Version of the basic ISidedInventory that is designed to be used as a replacement for
 * the conventional inventory used in machines.
 *
 * @author Darkguardsman
 */
public class ExternalInventory extends BasicInventory implements IExternalInventory, ISidedInventory
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
            ((TileEntity)host).markDirty();
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
}
