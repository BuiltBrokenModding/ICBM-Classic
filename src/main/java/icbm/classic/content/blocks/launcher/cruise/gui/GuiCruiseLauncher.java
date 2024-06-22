package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.DisableButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCruiseLauncher extends GuiContainerBase
{
    // Localizations
    private static final String LANG_KEY = "gui.launcher.cruise";
    private static final String GUI_NAME = LANG_KEY + ".name";

    // Texture
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_cruise_launcher.png");

    // Launcher
    private final TileCruiseLauncher tileEntity;

    public GuiCruiseLauncher(EntityPlayer player, TileCruiseLauncher tileEntity)
    {
        super(new ContainerCruiseLauncher(player, tileEntity));
        this.tileEntity = tileEntity;
        this.height = 166;
        this.width = 175;
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

        // Target field
        addComponent(TextInput.vec3dField(componentID++, fontRenderer, 18, 17, 100, 12,
            tileEntity::getTarget, tileEntity::setTarget, (o) -> TileCruiseLauncher.PACKET_TARGET.sendToServer(tileEntity)));
        addComponent(TextInput.textField(componentID++, fontRenderer, 135, 17, 34, 12,
            tileEntity.radio::getChannel, tileEntity.radio::setChannel, (o) -> TileCruiseLauncher.PACKET_RADIO_HZ.sendToServer(tileEntity)));

        // Launch button
        addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(() -> this.tileEntity.getLauncher().preCheckLaunch(new BasicTargetData(tileEntity.getTarget()), null).message()))
            .setAction(() -> TileCruiseLauncher.PACKET_LAUNCH.sendToServer(tileEntity))
            .setEnabledCheck(() -> !tileEntity.getLauncher().preCheckLaunch(new BasicTargetData(tileEntity.getTarget()), null).isBlocking())
        ;

        addComponent(new SlotEnergyBar(141, 66,
            tileEntity.energyStorage::getEnergyStored,
            tileEntity.energyStorage::getMaxEnergyStored)
            .withActionCost(tileEntity::getFiringCost)
        );

        // Radio tooltip
        addComponent(new TooltipTranslations(119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));
        addComponent(new DisableButton(1, guiLeft + 119, guiTop + 16, tileEntity.radio::isDisabled)
            .setAction(() -> TileCruiseLauncher.PACKET_RADIO_DISABLE.sendToServer(tileEntity))
        );

        // Target tooltip
        addComponent(new TooltipTranslations(2, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_TARGET).withDelay(1));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        // Draw text
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal(GUI_NAME), 52, 6, 4210752);
        this.fontRenderer.drawString(LanguageUtility.getLocal("container.inventory"), 8, this.ySize - 96 + 4, 4210752);

        // Goes last so tooltips render above our UI elements
        super.drawGuiContainerForegroundLayer(par1, par2);
    }
}
