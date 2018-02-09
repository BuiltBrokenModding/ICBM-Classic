package icbm.classic.content.machines.coordinator;

import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerMissileCoordinator extends ContainerBase
{
    public ContainerMissileCoordinator(EntityPlayer player, TileMissileCoordinator tileEntity)
    {
        super(player, tileEntity);
        this.addSlotToContainer(new Slot(tileEntity.getInventory(), 0, 16, 41));
        this.addSlotToContainer(new Slot(tileEntity.getInventory(), 1, 136, 41));
        this.addPlayerInventory(player, 8 , 135);
    }
}
