package icbm.classic.prefab.gui;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class ContainerBase<H extends Object> extends Container {

    protected EntityPlayer player;
    protected H host;

    public ContainerBase(EntityPlayer player, H node) {
        this.player = player;
        this.host = node;

        if (node instanceof IPlayerUsing) {
            ((IPlayerUsing) node).addPlayerToUseList(player);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        if (host instanceof IPlayerUsing && entityplayer.openContainer != this) {
            ((IPlayerUsing) host).removePlayerToUseList(entityplayer);
        }
        super.onContainerClosed(entityplayer);
    }

    public void addPlayerInventory(EntityPlayer player) {
        addPlayerInventory(player, 8, 84);
    }

    public void addPlayerInventory(EntityPlayer player, int x, int y) {

        //Inventory
        for (int row = 0; row < 3; ++row) {
            for (int slot = 0; slot < 9; ++slot) {
                this.addSlotToContainer(new Slot(player.inventory, slot + row * 9 + 9, slot * 18 + x, row * 18 + y));
            }
        }

        //Hot bar
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlotToContainer(new Slot(player.inventory, slot, slot * 18 + x, 58 + y));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {

        if (!(this.host instanceof TileEntity)) {
            ICBMClassic.logger().warn(String.format(
                this + "%s: dev bug... host node is missing! Unable to check ContainerBase#canInteractWith(%s) near '%s'",
                this,
                entityplayer.getName(),
                entityplayer.getPosition()
            ));
            return false;
        }

        // Overkill safety check to prevent inventory access in cases that could result in duplicate bugs
        if (!isHostValid() || !isAreaLoaded() || ((TileEntity) this.host).getWorld() != entityplayer.world) {
            return false;
        }

        final BlockPos pos = ((TileEntity) this.host).getPos();
        return entityplayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 4.0;
    }

    protected boolean isAreaLoaded() {
        return this.host instanceof TileEntity
            && ((TileEntity) this.host).getWorld().isBlockLoaded(((TileEntity) this.host).getPos());
    }

    protected boolean isHostValid() {
        return this.host instanceof TileEntity && !((TileEntity) this.host).isInvalid();
    }
}