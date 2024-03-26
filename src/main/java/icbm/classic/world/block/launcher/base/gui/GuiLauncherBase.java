package icbm.classic.world.block.launcher.base.gui;

import icbm.classic.IcbmConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.FaceRotationButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import icbm.classic.world.block.launcher.base.LauncherBaseBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiLauncherBase extends GuiContainerBase {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IcbmConstants.MOD_ID, IcbmConstants.GUI_DIRECTORY + "gui_silo_base.png");
    public static final Component LOCK_HEIGHT_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.lock_height");
    public static final Component GROUP_ID_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.group_id");
    public static final Component GROUP_INDEX_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.group_index");
    public static final Component FIRING_DELAY_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.firing_delay");

    private final LauncherBaseBlockEntity tileEntity;

    public GuiLauncherBase(Player player, LauncherBaseBlockEntity tileEntity) {
        super(new ContainerLaunchBase(player, tileEntity));
        this.tileEntity = tileEntity;
        ySize = 166;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui() {
        super.initGui();

        int componentID = 0;

        // Target field
        addComponent(TextInput.intField(componentID++, fontRenderer, 17, 17, 30, 12,
            tileEntity::getLockHeight, tileEntity::setLockHeight, (o) -> LauncherBaseBlockEntity.PACKET_LOCK_HEIGHT.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, fontRenderer, 82, 17, 30, 12,
            tileEntity::getGroupId, tileEntity::setGroupId, (o) -> LauncherBaseBlockEntity.PACKET_GROUP_ID.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, fontRenderer, 127, 17, 30, 12,
            tileEntity::getGroupIndex, tileEntity::setGroupIndex, (o) -> LauncherBaseBlockEntity.PACKET_GROUP_INDEX.sendToServer(tileEntity)));
        addComponent(TextInput.intField(componentID++, fontRenderer, 17, 17 + 16, 30, 12,
            tileEntity::getFiringDelay, tileEntity::setFiringDelay, (o) -> LauncherBaseBlockEntity.PACKET_FIRING_DELAY.sendToServer(tileEntity)));

        addComponent(new FaceRotationButton(0, guiLeft + 157, guiTop + 3, tileEntity::getLaunchDirection, tileEntity::getSeatSide, tileEntity::setSeatSide,
            () -> LauncherBaseBlockEntity.PACKET_SEAT_ROTATION.sendToServer(tileEntity)));

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

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("gui.icbmclassic:launcherbase.name"), 30, 6, 4210752);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
