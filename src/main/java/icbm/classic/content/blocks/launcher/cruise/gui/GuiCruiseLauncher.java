package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.gui.GuiFormatHelpers;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.tooltip.Tooltip;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;

import static java.lang.Integer.parseInt;

public class GuiCruiseLauncher extends GuiContainerBase
{
    // Localizations
    private static final String LANG_KEY = "gui.launcher.cruise";
    private static final String GUI_NAME = LANG_KEY + ".name";

    //UV
    final int ENERGY_BAR_WIDTH = 16;
    final int ENERGY_BAR_HEIGHT = 2;

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
    protected ResourceLocation getBackground() {
        return TEXTURE;
    }

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
            .setAction(() -> ICBMClassic.packetHandler.sendToServer(new PacketTile("launch_C>S", TileCruiseLauncher.LAUNCH_PACKET_ID, this.tileEntity)))
            .setEnabledCheck(tileEntity::canLaunch)
        ;

        // Energy bar
        addComponent(new Tooltip(new Rectangle(141, 66, 141 + ENERGY_BAR_WIDTH, 66 + ENERGY_BAR_HEIGHT), () -> String.format("%s / %s FE", tileEntity.getEnergy(), tileEntity.getEnergyBufferSize())));
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

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);

        // Draw features
        drawEnergyBar();
    }

    protected void drawEnergyBar() {

        this.mc.renderEngine.bindTexture(this.getBackground());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float energyPercent = tileEntity.getEnergy() / (float)tileEntity.getEnergyBufferSize();
        final float barRatio = (float)Math.floor(ENERGY_BAR_WIDTH * energyPercent);
        final int minBar = tileEntity.getEnergy() > 0 ? 1 : 0;
        int renderWidth = (int)Math.min(Math.max(minBar, barRatio), ENERGY_BAR_WIDTH);
        this.drawTexturedModalRect(guiLeft + 141, guiTop + 66, 256 - ENERGY_BAR_WIDTH, 0, renderWidth, ENERGY_BAR_HEIGHT);
    }
}
