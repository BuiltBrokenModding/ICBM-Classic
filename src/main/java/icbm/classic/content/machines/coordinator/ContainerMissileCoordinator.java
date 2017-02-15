package icbm.classic.content.machines.coordinator;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerMissileCoordinator extends ContainerBase
{
    public ContainerMissileCoordinator(EntityPlayer player, TileMissileCoordinator tileEntity)
    {
        super(tileEntity);
        this.addSlotToContainer(new Slot(tileEntity, 0, 16, 41));
        this.addSlotToContainer(new Slot(tileEntity, 1, 136, 41));
        this.addPlayerInventory(player, 8 , 135);
    }
}
