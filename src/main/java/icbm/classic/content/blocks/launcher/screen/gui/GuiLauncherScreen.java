package icbm.classic.content.blocks.launcher.screen.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.Tooltip;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;

@SideOnly(Side.CLIENT)
public class GuiLauncherScreen extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_silo_screen.png");

    private final TileLauncherScreen tileEntity;

    public GuiLauncherScreen(EntityPlayer player, TileLauncherScreen tileEntity)
    {
        super(new ContainerLaunchScreen(player, tileEntity));
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
        addComponent(TextInput.vec3dField(componentID++, fontRenderer, 18, 17, 100, 12,
            tileEntity::getTarget, tileEntity::setTarget, tileEntity::sendTargetPacket));
        addComponent(TextInput.textField(componentID++, fontRenderer, 135, 17, 34, 12,
            tileEntity.radioCap::getChannel, tileEntity.radioCap::setChannel, tileEntity::sendHzPacket));

        // Launch button
        addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(this.tileEntity::getStatusTranslation))
            .setAction(tileEntity::sendLaunchPacket)
            .setEnabledCheck(tileEntity::canLaunch);

        addComponent(new SlotEnergyBar(141, 66, tileEntity::getEnergy, tileEntity::getEnergyBufferSize));

        addComponent(new Tooltip(new Rectangle(60, 32, 60 + 30, 32 + 12), () -> new TextComponentTranslation("gui.launcherscreen.inaccuracy.info"), 1));
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
