package icbm.classic.content.machines.battery;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2018.
 */
public class GuiBattery extends GuiContainerBase
{
    public GuiBattery(EntityPlayer player, TileEntityBattery battery)
    {
        super(new ContainerBattery(player, battery));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
        for(Slot slot : inventorySlots.inventorySlots)
        {
            drawSlot(slot);
        }
    }
}
