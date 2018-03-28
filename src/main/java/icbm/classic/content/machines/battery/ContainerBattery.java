package icbm.classic.content.machines.battery;

import icbm.classic.prefab.gui.ContainerBase;
import icbm.classic.prefab.gui.slot.SlotEnergyItem;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2018.
 */
public class ContainerBattery extends ContainerBase<TileEntityBattery>
{
    public ContainerBattery(EntityPlayer player, TileEntityBattery node)
    {
        super(player, node);
        int x = 50;
        int y = 50;
        for (int slot = 0; slot < TileEntityBattery.SLOTS; slot++)
        {
            this.addSlotToContainer(new SlotEnergyItem(node.getInventory(), slot, slot * 18 + x, y));
        }

        addPlayerInventory(player);
    }
}
