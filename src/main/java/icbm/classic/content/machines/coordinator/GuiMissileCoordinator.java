package icbm.classic.content.machines.coordinator;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.api.items.tools.IWorldPosItem;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.imp.transform.vector.Location;
import icbm.classic.content.gui.GuiICBMContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;

public class GuiMissileCoordinator extends GuiICBMContainer
{
    private TileMissileCoordinator tileEntity;
    private float animation = 0;

    public GuiMissileCoordinator(EntityPlayer player, TileMissileCoordinator tileEntity)
    {
        super(new ContainerMissileCoordinator(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 220;
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString("\u00a77" + tileEntity.getInventoryName(), 48, 6, 4210752);
        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.sim"), 50, 20, 4210752);
        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.from"), 13, 30, 4210752);
        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.to"), 134, 30, 4210752);

        if (this.tileEntity.getStackInSlot(0) != null && this.tileEntity.getStackInSlot(1) != null)
        {
            if (this.tileEntity.getStackInSlot(0).getItem() instanceof IWorldPosItem && this.tileEntity.getStackInSlot(1).getItem() instanceof IWorldPosItem)
            {
                Location pos1 = new Location(((IWorldPosItem) this.tileEntity.getStackInSlot(0).getItem()).getLocation(this.tileEntity.getStackInSlot(0)));
                Location pos2 = new Location(((IWorldPosItem) this.tileEntity.getStackInSlot(1).getItem()).getLocation(this.tileEntity.getStackInSlot(1)));

                double displacement = pos1.distance(pos2);

                this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.displace").replaceAll("%p", "" + UnitDisplay.roundDecimals(displacement)), 13, 65, 4210752);

                double w = pos1.toVector2().distance(pos2.toVector2());
                double h = 160 + (w * 3) - pos1.y();

                double distance = 0.5 * Math.sqrt(16 * (h * h) + (w * w)) + (((w * w) / (8 * h)) * (Math.log(4 * h + Math.sqrt(16 * (h * h) + (w * w))) - Math.log(w)));

                this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.arc").replaceAll("%p", "" + UnitDisplay.roundDecimals(distance)), 13, 75, 4210752);
                this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.time").replaceAll("%p", "" + UnitDisplay.roundDecimals(Math.max(100, 2 * displacement) / 20)), 13, 85, 4210752);

                Location delta = pos1.subtract(pos2);
                double rotation = MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(delta.z(), delta.x()))) - 90;
                int heading = MathHelper.floor_double(rotation * 4.0F / 360.0F + 0.5D) & 3;

                this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.direction") + " " + UnitDisplay.roundDecimals(rotation) + " (" + Direction.directions[heading] + ")", 13, 95, 4210752);
            }
        }

        //this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.coordinator.wip"), 13, 120, 4210752);

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.drawSlot(15, 40);
        this.drawSlot(135, 40);

        //this.drawBar(75, 40, 1 - this.animation);

        if (this.tileEntity.getStackInSlot(0) != null && this.tileEntity.getStackInSlot(1) != null)
        {
            this.animation = (this.animation + 0.005f * f) % 1;

        }
        else
        {
            this.animation = 1;
        }
    }
}
