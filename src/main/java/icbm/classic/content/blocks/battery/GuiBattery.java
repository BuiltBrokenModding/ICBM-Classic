package icbm.classic.content.blocks.battery;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2018.
 */
public class GuiBattery extends GuiContainerBase
{
    protected final TileEntityBattery battery;

    public GuiBattery(EntityPlayer player, TileEntityBattery battery)
    {
        super(new ContainerBattery(player, battery));
        this.battery = battery;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
        for (Slot slot : inventorySlots.inventorySlots)
        {
            drawSlot(slot);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        int energy = battery._localEnergy;
        int energyMax = battery._localEnergyMax;
        this.fontRenderer.drawString("Energy: " + energy + "/" + energyMax, 12, 70, 4210752);
    }
}
