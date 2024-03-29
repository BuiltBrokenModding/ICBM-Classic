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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLauncherScreen extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_screen.png");
    public static final ITextComponent ACCURACY_TOOLTIP = new TextComponentTranslation("gui.launcherscreen.inaccuracy.info");

    private final TileLauncherScreen tileEntity;

    public GuiLauncherScreen(EntityPlayer player, TileLauncherScreen tileEntity)
    {
        super(new ContainerLaunchScreen(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 166;
        this.xSize = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    /** Adds the buttons (and other controls) to the screen in question. */
    @Override
    public void initGui()
    {
        super.initGui();

        int componentID = 0;

        // Target field
        addComponent(TextInput.vec3dField(componentID++, fontRenderer, 18, 17, 100, 12,
            tileEntity::getTarget, tileEntity::setTarget, (o) -> TileLauncherScreen.PACKET_TARGET.sendToServer(tileEntity)));

        // Hz
        addComponent(TextInput.textField(componentID++, fontRenderer, 135, 17, 34, 12,
            tileEntity.radioCap::getChannel, tileEntity.radioCap::setChannel, (o) -> TileLauncherScreen.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // Launch button
        addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(this.tileEntity::getStatusTranslation))
            .setAction(() -> TileLauncherScreen.PACKET_LAUNCH.sendToServer(tileEntity))
            .setEnabledCheck(tileEntity::canLaunch);

        addComponent(new SlotEnergyBar(141, 66, tileEntity.energyStorage::getEnergyStored, tileEntity.energyStorage::getMaxEnergyStored));

        addComponent(new TooltipTranslations(60, 32, 30, 12, ACCURACY_TOOLTIP).withDelay(1));

        // Radio tooltip
        addComponent(new TooltipTranslations(119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
        addComponent(new DisableButton(1, guiLeft + 119, guiTop + 16, tileEntity.radioCap::isDisabled)
            .setAction(() -> TileLauncherScreen.PACKET_RADIO_DISABLE.sendToServer(tileEntity))
        );

        // Target tooltip
        addComponent(new TooltipTranslations(2, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_TARGET).withDelay(1));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("gui.launcherscreen.name"), 30, 6, 4210752);
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.launcherscreen.inaccuracy").replaceAll("%1\\$s", String.format("%.2f", tileEntity.getLauncherInaccuracy())), 60, 32, 4210752);

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
