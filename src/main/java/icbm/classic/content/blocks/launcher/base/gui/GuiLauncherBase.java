package icbm.classic.content.blocks.launcher.base.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLauncherBase extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_base.png");
    public static final ITextComponent LOCK_HEIGHT_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.lock_height");
    public static final ITextComponent GROUP_ID_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.group_id");
    public static final ITextComponent GROUP_INDEX_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.group_index");
    public static final ITextComponent FIRING_DELAY_TOOLTIP = new TextComponentTranslation("gui.icbmclassic:launcherbase.firing_delay");

    private final TileLauncherBase tileEntity;

    public GuiLauncherBase(EntityPlayer player, TileLauncherBase tileEntity)
    {
        super(new ContainerLaunchBase(player, tileEntity));
        this.tileEntity = tileEntity;
        ySize = 166;
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
        addComponent(TextInput.intField(componentID++, fontRenderer, 17, 17, 30, 12,
            tileEntity::getLockHeight, tileEntity::setLockHeight, tileEntity::sendLockHeightPacket));
        addComponent(TextInput.intField(componentID++, fontRenderer, 82, 17, 30, 12,
            tileEntity::getGroupId, tileEntity::setGroupId, tileEntity::sendGroupIdPacket));
        addComponent(TextInput.intField(componentID++, fontRenderer, 127, 17, 30, 12,
            tileEntity::getGroupIndex, tileEntity::setGroupIndex, tileEntity::sendGroupIndexPacket));
        addComponent(TextInput.intField(componentID++, fontRenderer, 17, 17 + 16, 30, 12,
            tileEntity::getFiringDelay, tileEntity::setFiringDelay, tileEntity::sendFiringDelayPacket));

        addComponent(new SlotEnergyBar(141, 66, tileEntity.energyStorage::getEnergyStored, tileEntity.energyStorage::getMaxEnergyStored));

        addComponent(new TooltipTranslations(4, 16, 14, 14, LOCK_HEIGHT_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(68, 16, 14, 14, GROUP_ID_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(113, 16, 14, 14, GROUP_INDEX_TOOLTIP).withDelay(1));
        addComponent(new TooltipTranslations(4, 16 + 16, 14, 14, FIRING_DELAY_TOOLTIP).withDelay(1));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("gui.icbmclassic:launcherbase.name"), 30, 6, 4210752);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
