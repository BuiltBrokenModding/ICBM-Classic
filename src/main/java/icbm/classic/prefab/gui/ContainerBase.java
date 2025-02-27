package icbm.classic.prefab.gui;

import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class ContainerBase<H extends Object> extends Container
{
    protected int slotCount = 0;

    protected IInventory inventory;
    protected PlayerEntity player;

    @Getter
    private H host;

    public ContainerBase(@Nullable ContainerType<?> type, int id, IInventory inventory)
    {
        super(type, id);
        this.inventory = inventory;
        this.slotCount = inventory.getSizeInventory();
    }

    public ContainerBase(@Nullable ContainerType<?> type, int id, PlayerEntity player, H node)
    {
        super(type, id);
        this.host = node;
        this.player = player;

        if (node instanceof IInventory)
        {
            inventory = (IInventory) node;
        }
        if (node instanceof IPlayerUsing)
        {
            ((IPlayerUsing) node).addPlayerToUseList(player);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity entityplayer)
    {
        if (host instanceof IPlayerUsing && entityplayer.openContainer != this)
        {
            ((IPlayerUsing) host).removePlayerToUseList(entityplayer);
        }
        super.onContainerClosed(entityplayer);
    }

    public void addPlayerInventory(PlayerEntity player)
    {
        addPlayerInventory(player, 8, 84);
    }

    public void addPlayerInventory(PlayerEntity player, int x, int y)
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
                this.addSlot(new net.minecraft.inventory.container.Slot(player.inventory, slot + row * 9 + 9, slot * 18 + x, row * 18 + y));
            }
        }

        //Hot bar
        for (int slot = 0; slot < 9; ++slot)
        {
            this.addSlot(new Slot(player.inventory, slot, slot * 18 + x, 58 + y));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity entityplayer)
    {
        if(this.inventory != null) {
            return this.inventory.isUsableByPlayer(entityplayer);
        }
        else if(this.host instanceof TileEntity) {
            final BlockPos pos = ((TileEntity) this.host).getPos();
            return Math.sqrt(entityplayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()+ 0.5)) <= 4.0;
        }
        return true;
    }
}