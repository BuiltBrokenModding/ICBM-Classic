package icbm.classic.prefab.gui;

import icbm.classic.prefab.inventory.IInventoryProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ContainerBase<H extends Object> extends Container
{
    protected int slotCount = 0;

    protected IInventory inventory;
    protected EntityPlayer player;
    protected H host;

    public ContainerBase(IInventory inventory)
    {
        this.inventory = inventory;
        this.slotCount = inventory.getSizeInventory();
    }

    @Deprecated
    public ContainerBase(EntityPlayer player, IInventory inventory)
    {
        this(inventory);

        this.player = player;
        if (inventory instanceof IPlayerUsing)
        {
            ((IPlayerUsing) inventory).getPlayersUsing().add(player);
        }
    }

    public ContainerBase(EntityPlayer player, H node)
    {
        if (node instanceof IInventory)
        {
            inventory = (IInventory) node;
        }
        else if (node instanceof IInventoryProvider)
        {
            inventory = ((IInventoryProvider) node).getInventory();
        }

        this.player = player;
        if (node instanceof IPlayerUsing)
        {
            ((IPlayerUsing) node).addPlayerToUseList(player);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
        if (host instanceof IPlayerUsing && entityplayer.openContainer != this)
        {
            ((IPlayerUsing) host).removePlayerToUseList(entityplayer);
        }
        super.onContainerClosed(entityplayer);
    }

    public void addPlayerInventory(EntityPlayer player)
    {
        addPlayerInventory(player, 8, 84);
    }

    public void addPlayerInventory(EntityPlayer player, int x, int y)
    {
        if (this.inventory instanceof IPlayerUsing)
        {
            ((IPlayerUsing) this.inventory).getPlayersUsing().add(player);
        }

        //Inventory
        for (int row = 0; row < 3; ++row)
        {
            for (int slot = 0; slot < 9; ++slot)
            {
                this.addSlotToContainer(new Slot(player.inventory, slot + row * 9 + 9, slot * 18 + x, row * 18 + y));
            }
        }

        //Hot bar
        for (int slot = 0; slot < 9; ++slot)
        {
            this.addSlotToContainer(new Slot(player.inventory, slot, slot * 18 + x, 58 + y));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        if(this.inventory != null) {
            return this.inventory.isUsableByPlayer(entityplayer);
        }
        else if(this.host instanceof TileEntity) {
            final BlockPos pos = ((TileEntity) this.host).getPos();
            return entityplayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()+ 0.5) <= 4.0;
        }
        return true;
    }
}