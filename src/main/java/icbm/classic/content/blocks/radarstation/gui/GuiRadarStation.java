package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiRadarStation extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_radar.png");

    private final TileRadarStation tileEntity;

    public GuiRadarStation(EntityPlayer player, TileRadarStation tileEntity)
    {
        super(new ContainerRadarStation(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 184;
        this.xSize = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int componentID = 0;

        // Hz
        addComponent(TextInput.textField(componentID++, fontRenderer, 135, 17, 34, 12,
            tileEntity.getRadio()::getChannel, tileEntity.getRadio()::setChannel, tileEntity::sendHzPacket));

        // trigger
        addComponent(TextInput.intField(componentID++, fontRenderer, 18, 77, 29, 12,
            tileEntity::getTriggerRange, tileEntity::setTriggerRange, tileEntity::sendTriggerRangePacket));

        // detection
        addComponent(TextInput.intField(componentID++, fontRenderer, 49, 77, 29, 12,
            tileEntity::getDetectionRange, tileEntity::setDetectionRange, tileEntity::sendDetectionRangePacket));

        addComponent(new SlotEnergyBar(141, 66, tileEntity::getEnergy, tileEntity::getEnergyBufferSize));
        addComponent(new RadarComponent(tileEntity, 5, 18));

        addComponent(new TooltipTranslations(4, 76, 14, 14, TileRadarStation.TRANSLATION_TOOLTIP_RANGE).withShift(TileRadarStation.TRANSLATION_TOOLTIP_RANGE_SHIFT).withDelay(1));

    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + tileEntity.getDisplayName().getFormattedText(), 30, 6, 4210752);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
