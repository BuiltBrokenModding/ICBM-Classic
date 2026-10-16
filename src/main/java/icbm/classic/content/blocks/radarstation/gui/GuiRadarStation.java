package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.RedstoneButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRadarStation extends GuiContainerBase<ContainerRadarStation>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_radar.png");

    private final TileRadarStation tileEntity;

    public GuiRadarStation(ContainerRadarStation container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.tileEntity = container.getHost();
        this.imageHeight = 184;
        this.imageWidth = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    @Override
    protected void init()
    {
        super.init();

        int componentID = 0;

        // Hz
        addComponent(TextInput.textField(componentID++, font, 135, 17, 34, 12,
            tileEntity.getRadio()::getChannel, tileEntity.getRadio()::setChannel, (o) -> TileRadarStation.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // trigger
        addComponent(TextInput.intField(componentID++, font, 18, 77, 29, 12,
            tileEntity::getTriggerRange, tileEntity::setTriggerRange, (o) -> TileRadarStation.PACKET_TRIGGER_RANGE.sendToServer(tileEntity)));

        // detection
        addComponent(TextInput.intField(componentID++, font, 49, 77, 29, 12,
            tileEntity::getDetectionRange, tileEntity::setDetectionRange, (o) -> TileRadarStation.PACKET_DETECTION_RANGE.sendToServer(tileEntity)));

        addComponent(
            new RedstoneButton(0, 160 + leftPos, 3 + topPos, tileEntity::isOutputRedstone)
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

    /** Draw the foreground layer (everything in front of the items) */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        drawString(graphics, "§7" + tileEntity.getDisplayName().getString(), 30, 6, 4210752);
        super.renderLabels(graphics, mouseX, mouseY);
    }
}
// PORTED
