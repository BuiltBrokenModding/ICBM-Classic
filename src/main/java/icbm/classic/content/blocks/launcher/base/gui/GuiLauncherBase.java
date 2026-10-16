package icbm.classic.content.blocks.launcher.base.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.FaceRotationButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiLauncherBase extends GuiContainerBase<ContainerLaunchBase>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_base.png");
    public static final Component LOCK_HEIGHT_TOOLTIP = Component.translatable("gui.icbmclassic:launcherbase.lock_height");
    public static final Component GROUP_ID_TOOLTIP = Component.translatable("gui.icbmclassic:launcherbase.group_id");
    public static final Component GROUP_INDEX_TOOLTIP = Component.translatable("gui.icbmclassic:launcherbase.group_index");
    public static final Component FIRING_DELAY_TOOLTIP = Component.translatable("gui.icbmclassic:launcherbase.firing_delay");

    private final TileLauncherBase tileEntity;

    public GuiLauncherBase(ContainerLaunchBase container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.tileEntity = container.getHost();
        imageHeight = 166;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    /** Adds the buttons (and other controls) to the screen in question. */
    @Override
    protected void init()
    {
        super.init();

        int componentID = 0;

        // Target field
        addComponent(TextInput.intField(componentID++, font, 17, 17, 30, 12,
            tileEntity::getLockHeight, tileEntity::setLockHeight, (o) -> TileLauncherBase.PACKET_LOCK_HEIGHT.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, font, 82, 17, 30, 12,
            tileEntity::getGroupId, tileEntity::setGroupId, (o) -> TileLauncherBase.PACKET_GROUP_ID.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, font, 127, 17, 30, 12,
            tileEntity::getGroupIndex, tileEntity::setGroupIndex, (o) -> TileLauncherBase.PACKET_GROUP_INDEX.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, font, 17, 17 + 16, 30, 12,
            tileEntity::getFiringDelay, tileEntity::setFiringDelay, (o) -> TileLauncherBase.PACKET_FIRING_DELAY.sendToServer(tileEntity)));

        addComponent(new FaceRotationButton(0, leftPos + 157, topPos + 3,  tileEntity::getLaunchDirection, tileEntity::getSeatSide, tileEntity::setSeatSide,
            () -> TileLauncherBase.PACKET_SEAT_ROTATION.sendToServer(tileEntity)));

        addComponent(new SlotEnergyBar(141, 66,
            tileEntity.energyStorage::getEnergyStored,
            tileEntity.energyStorage::getMaxEnergyStored
           ).withActionCost(tileEntity::getFiringCost)
        );

        addComponent(new TooltipTranslations(4, 16, 14, 14, LOCK_HEIGHT_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(68, 16, 14, 14, GROUP_ID_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(113, 16, 14, 14, GROUP_INDEX_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(4, 16 + 16, 14, 14, FIRING_DELAY_TOOLTIP).withDelay(1));
    }

    /** Draw the foreground layer (everything in front of the items) */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        drawString(graphics, "§7" + LanguageUtility.getLocal("gui.icbmclassic:launcherbase.name"), 30, 6, 4210752);
        super.renderLabels(graphics, mouseX, mouseY);
    }
}
// PORTED
