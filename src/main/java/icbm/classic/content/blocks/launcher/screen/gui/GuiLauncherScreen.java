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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiLauncherScreen extends GuiContainerBase<ContainerLaunchScreen> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_screen.png");
    public static final ITextComponent ACCURACY_TOOLTIP = new TranslationTextComponent("gui.launcherscreen.inaccuracy.info");

    public GuiLauncherScreen(ContainerLaunchScreen container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        this.ySize = 166;
        this.xSize = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void init() {
        super.init();

        // Target field
        addButton(TextInput.vec3dField(font, guiLeft + 18, guiTop + 17, 100, 12,
            this.getContainer().getHost()::getTarget, this.getContainer().getHost()::setTarget, (o) -> TileLauncherScreen.PACKET_TARGET.sendToServer(this.getContainer().getHost())));

        // Hz
        addButton(TextInput.textField(font, guiLeft + 135, guiTop + 17, 34, 12,
            this.getContainer().getHost().radioCap::getChannel, this.getContainer().getHost().radioCap::setChannel, (o) -> TileLauncherScreen.PACKET_RADIO_HZ.sendToServer(this.getContainer().getHost())));

        // Launch button
        addButton(new LaunchButton(guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(this.getContainer().getHost()::getStatusTranslation))
            .setAction(() -> TileLauncherScreen.PACKET_LAUNCH.sendToServer(this.getContainer().getHost()))
            .setEnabledCheck(this.getContainer().getHost()::canLaunch);

        addButton(new SlotEnergyBar(guiLeft + 141, guiTop + 66,
            this.getContainer().getHost().energyStorage::getEnergyStored,
            this.getContainer().getHost().energyStorage::getMaxEnergyStored,
            TEXTURE
        ));

        addButton(new TooltipTranslations(this, 60, 32, 30, 12, ACCURACY_TOOLTIP).withDelay(1));

        // Radio tooltip
        addButton(new TooltipTranslations(this, 119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
        addButton(new DisableButton(guiLeft + 119, guiTop + 16, I18n.format(ICBMConstants.PREFIX + "button.disable.machine"), this.getContainer().getHost().radioCap::isDisabled)
            .setAction(() -> TileLauncherScreen.PACKET_RADIO_DISABLE.sendToServer(this.getContainer().getHost()))
        );

        // Target tooltip
        addButton(new TooltipTranslations(this, 2, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_TARGET).withDelay(1));
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString("\u00a77" + LanguageUtility.getLocal("gui.launcherscreen.name"), 30, 6, 4210752);
        this.font.drawString(LanguageUtility.getLocal("gui.launcherscreen.inaccuracy").replaceAll("%1\\$s", String.format("%.2f", this.getContainer().getHost().getLauncherInaccuracy())), 60, 32, 4210752);

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
