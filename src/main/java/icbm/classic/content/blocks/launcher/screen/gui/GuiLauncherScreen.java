package icbm.classic.content.blocks.launcher.screen.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.DisableButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiLauncherScreen extends GuiContainerBase<ContainerLaunchScreen>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_screen.png");
    public static final Component ACCURACY_TOOLTIP = Component.translatable("gui.launcherscreen.inaccuracy.info");

    private final TileLauncherScreen tileEntity;

    public GuiLauncherScreen(ContainerLaunchScreen container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.tileEntity = container.getHost();
        this.imageHeight = 166;
        this.imageWidth = 175;
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
        addComponent(TextInput.vec3dField(componentID++, font, 18, 17, 100, 12,
            tileEntity::getTarget, tileEntity::setTarget, (o) -> TileLauncherScreen.PACKET_TARGET.sendToServer(tileEntity)));

        // Hz
        addComponent(TextInput.textField(componentID++, font, 135, 17, 34, 12,
            tileEntity.radioCap::getChannel, tileEntity.radioCap::setChannel, (o) -> TileLauncherScreen.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // Launch button
        addComponent(new LaunchButton(0, leftPos + 24, topPos + 38)
            .doDrawDisabledGlass()
            .setTooltip(this.tileEntity::getStatusTranslation)
            .setAction(() -> TileLauncherScreen.PACKET_LAUNCH.sendToServer(tileEntity))
            .setEnabledCheck(tileEntity::canLaunch));

        addComponent(new SlotEnergyBar(141, 66, tileEntity.energyStorage::getEnergyStored, tileEntity.energyStorage::getMaxEnergyStored));

        addComponent(new TooltipTranslations(60, 32, 30, 12, ACCURACY_TOOLTIP).withDelay(1));

        // Radio tooltip
        addComponent(new TooltipTranslations(119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
        addComponent(new DisableButton(1, leftPos + 119, topPos + 16, tileEntity.radioCap::isDisabled)
            .setAction(() -> TileLauncherScreen.PACKET_RADIO_DISABLE.sendToServer(tileEntity))
        );

        // Target tooltip
        addComponent(new TooltipTranslations(2, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_TARGET).withDelay(1));
    }

    /** Draw the foreground layer (everything in front of the items) */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        drawString(graphics, "§7" + LanguageUtility.getLocal("gui.launcherscreen.name"), 30, 6, 4210752);
        drawString(graphics, LanguageUtility.getLocal("gui.launcherscreen.inaccuracy").replaceAll("%1\\$s", String.format("%.2f", tileEntity.getLauncherInaccuracy())), 60, 32, 4210752);

        super.renderLabels(graphics, mouseX, mouseY);
    }
}
// PORTED
