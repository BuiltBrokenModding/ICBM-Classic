package icbm.classic.content.blocks.launcher.base.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.Tooltip;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLauncherBase extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_base.png");

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
            tileEntity::getGroup, tileEntity::setGroup, tileEntity::sendGroupIdPacket));
        addComponent(TextInput.intField(componentID++, fontRenderer, 127, 17, 30, 12,
            tileEntity::getGroupIndex, tileEntity::setGroupIndex, tileEntity::sendGroupIndexPacket));

        addComponent(new SlotEnergyBar(141, 66, tileEntity::getEnergy, tileEntity::getEnergyBufferSize));

        addComponent(new Tooltip(new Rectangle(4, 16, 4 + 14, 16 + 14), () -> new TextComponentTranslation("gui.icbmclassic:launcherbase.lock_height"), 1));
        addComponent(new Tooltip(new Rectangle(68, 16, 68 + 14, 16 + 14), () -> new TextComponentTranslation("gui.icbmclassic:launcherbase.group_id"), 1));
        addComponent(new Tooltip(new Rectangle(113, 16, 113 + 14, 16 + 14), () -> new TextComponentTranslation("gui.icbmclassic:launcherbase.group_index"), 1));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("gui.icbmclassic:launcherbase.name"), 30, 6, 4210752);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
