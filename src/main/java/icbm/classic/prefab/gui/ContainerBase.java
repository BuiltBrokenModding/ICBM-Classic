package icbm.classic.prefab.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerBase<H> implements Container {
    protected int slotCount = 0;

    protected Container container;
    protected Player player;
    protected H host;

    public ContainerBase(int id, MenuType<? extends ContainerBase<H>> type, Container container) {
        super(type, id);
        this.container = container;
        this.slotCount = container.getContainerSize();
    }

    @Deprecated
    public ContainerBase(int id, MenuType<? extends ContainerBase<H>> type, Player player, Container container) {
        super(type, id);

        this.player = player;
        if (container instanceof IPlayerUsing) {
            ((IPlayerUsing) container).getPlayersUsing().add(player);
        }
    }

    public ContainerBase(int id, MenuType<? extends ContainerBase<H>> type, Player player, H node) {
        super(type, id);
        if (node instanceof Container) {
            container = (Container) node;
        }

        this.player = player;
        if (node instanceof IPlayerUsing) {
            ((IPlayerUsing) node).addPlayerToUseList(player);
        }
    }

    @Override
    public void onContainerClosed(Player entityplayer) {
        if (host instanceof IPlayerUsing && entityplayer.openContainer != this) {
            ((IPlayerUsing) host).removePlayerToUseList(entityplayer);
        }
        super.onContainerClosed(entityplayer);
    }

    public void addPlayerInventory(Player player) {
        addPlayerInventory(player, 8, 84);
    }

    public void addPlayerInventory(Player player, int x, int y) {
        if (this.container instanceof IPlayerUsing) {
            ((IPlayerUsing) this.container).getPlayersUsing().add(player);
        }

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
    public boolean canInteractWith(Player entityplayer) {
        if (this.container != null) {
            return this.container.isUsableByPlayer(entityplayer);
        } else if (this.host instanceof BlockEntity) {
            final BlockPos pos = ((BlockEntity) this.host).getPos();
            return entityplayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 4.0;
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }
}