package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.RedstoneButton;
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
            tileEntity.getRadio()::getChannel, tileEntity.getRadio()::setChannel, (o) -> TileRadarStation.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // trigger
        addComponent(TextInput.intField(componentID++, fontRenderer, 18, 77, 29, 12,
            tileEntity::getTriggerRange, tileEntity::setTriggerRange, (o) -> TileRadarStation.PACKET_TRIGGER_RANGE.sendToServer(tileEntity)));

        // detection
        addComponent(TextInput.intField(componentID++, fontRenderer, 49, 77, 29, 12,
            tileEntity::getDetectionRange, tileEntity::setDetectionRange, (o) -> TileRadarStation.PACKET_DETECTION_RANGE.sendToServer(tileEntity)));

        addComponent(
            new RedstoneButton(0, 160 + guiLeft, 3 + guiTop, tileEntity::isOutputRedstone)
            .setAction(() -> TileRadarStation.PACKET_REDSTONE_OUTPUT.sendToServer(tileEntity))
                .setTooltip(() -> {
                    if(tileEntity.isOutputRedstone()) {
                        return TileRadarStation.TRANSLATION_TOOLTIP_REDSTONE_ON;
                    }
                    return TileRadarStation.TRANSLATION_TOOLTIP_REDSTONE_OFF;
                })
        );

        addComponent(new SlotEnergyBar(141, 66,
            tileEntity.energyStorage::getEnergyStored,
            tileEntity.energyStorage::getMaxEnergyStored)
            .withTickingCost(tileEntity::getEnergyCost)
        );
        addComponent(new RadarComponent(tileEntity, 5, 18));

        // Range tooltip
        addComponent(new TooltipTranslations(4, 76, 14, 14, TileRadarStation.TRANSLATION_TOOLTIP_RANGE).withShift(TileRadarStation.TRANSLATION_TOOLTIP_RANGE_SHIFT).withDelay(1));

        // Radio tooltip
        addComponent(new TooltipTranslations(119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + tileEntity.getDisplayName().getFormattedText(), 30, 6, 4210752);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
