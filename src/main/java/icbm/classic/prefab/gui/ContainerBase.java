package icbm.classic.prefab.gui;

import icbm.classic.api.tile.IPlayerUsing;
import icbm.classic.api.tile.provider.IInventoryProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

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
        return this.inventory.isUsableByPlayer(entityplayer);
    }
}