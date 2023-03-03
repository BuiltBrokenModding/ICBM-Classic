package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.gui.TextInput;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import java.io.IOException;

import static java.lang.Integer.parseInt;

public class GuiCruiseLauncher extends GuiContainerBase
{
    // Localizations
    private static final String LANG_KEY = "gui.launcher.cruise";
    private static final String GUI_NAME = LANG_KEY + ".name";
    private static final String LANG_ERROR = LANG_KEY + ".error";
    private static final String ERROR_NULL = LANG_ERROR + ".null";
    private static final String ERROR_FORMAT_TARGET = LANG_ERROR + ".format.target";

    //UV
    final int ENERGY_BAR_WIDTH = 16;
    final int ENERGY_BAR_HEIGHT = 2;

    // Texture
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_cruise_launcher.png");

    // Launcher
    private final TileCruiseLauncher tileEntity;

    // Components
    private TextInput fieldTarget;
    private TextInput fieldHz;
    private GuiButton launchButton;

    // User feedback
    private String targetError = null;
    private String hzError = null;

    public GuiCruiseLauncher(EntityPlayer player, TileCruiseLauncher tileEntity)
    {
        super(new ContainerCruiseLauncher(player, tileEntity));
        this.tileEntity = tileEntity;
        //166 tall, 175 wide
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int componentID = 0;

        // Target field
        this.fieldTarget = new TextInput(componentID++, fontRenderer, 18, 17, 100, 12);
        this.fieldTarget.setFocusChangedCallback((state) -> {
            if(!state) {

                // Reset previous error state
                this.targetError = null;

                // Parse input from user and store into tile client side
                storeTargetText();

                // Don't send or store if we have broken input
                if(targetError == null) {
                    sendTargetPacket();
                    updateTargetText();
                }

                // Enable error rendering
                this.fieldTarget.isErrored(targetError != null);
            }
        });
        this.tooltips.put(new Rectangle(18, 17, 18 + 100, 17 + 13), () -> targetError != null ? LanguageUtility.getLocal(targetError) : null);
        //TODO add validator to only allow numeric and commas

        // Frequency field
        this.fieldHz = new TextInput(componentID++,fontRenderer, 135, 17, 34, 12);
        this.fieldHz.setFocusChangedCallback((state) -> {
            if(!state) {
                // Reset previous error state
                this.hzError = null;

                // Parse input from user and store into tile client side
                storeHzText();

                // Don't send or store if we have broken input
                if(hzError == null) {
                    sendHzPacket();
                    updateHzText();
                }

                // Enable error rendering
                this.fieldHz.isErrored(hzError != null);
            }
        });
        this.tooltips.put(new Rectangle(135, 17, 135 + 34, 17 + 13), () -> hzError != null ? LanguageUtility.getLocal(hzError) : null);
        //TODO add validator for numeric only

        // Launch button
        this.launchButton = addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38));
        this.tooltips.put(new Rectangle(24, 38, 24 + 28, 38 + 29), () -> LanguageUtility.getLocal(this.tileEntity.getStatusTranslation()));

        // Energy bar
        this.tooltips.put(new Rectangle(141, 66, 141 + ENERGY_BAR_WIDTH, 66 + ENERGY_BAR_HEIGHT), () -> String.format("%s / %s FE", tileEntity.getEnergy(), tileEntity.getEnergyBufferSize()));
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        // Update from tile's data
        if(!fieldTarget.isFocused())
            updateTargetText();
        if(!fieldHz.isFocused())
            updateHzText();

        // only enable button if tile is ready to launch
        this.launchButton.enabled = this.tileEntity.canLaunch();
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        this.fieldTarget.textboxKeyTyped(par1, par2);
        this.fieldHz.textboxKeyTyped(par1, par2);
    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        this.fieldTarget.mouseClicked(par1 - guiLeft, par2 - guiTop, par3);
        this.fieldHz.mouseClicked(par1 - guiLeft, par2 - guiTop, par3);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if(button.id == launchButton.id)
            ICBMClassic.packetHandler.sendToServer(new PacketTile("launch_C>S", TileCruiseLauncher.LAUNCH_PACKET_ID, this.tileEntity));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        // Draw text
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal(GUI_NAME), 52, 6, 4210752);
        this.fontRenderer.drawString(LanguageUtility.getLocal("container.inventory"), 8, this.ySize - 96 + 4, 4210752);

        // Draw components
        this.fieldTarget.drawTextBox();
        this.fieldHz.drawTextBox();

        // Draw button cover
        if(!launchButton.enabled) {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            // TODO add animation to opening glass
            this.drawTexturedModalRect(20, 35, 220, 5, 36, 36);
        }

        // Goes last so tooltips render above our UI elements
        super.drawGuiContainerForegroundLayer(par1, par2);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        drawDefaultBackground();

        // Set color and texture
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Render background
        this.drawTexturedModalRect(guiLeft, guiTop,0, 0, this.xSize, this.ySize);

        // Draw features
        drawEnergyBar();
    }

    protected void drawEnergyBar() {

        float energyPercent = tileEntity.getEnergy() / (float)tileEntity.getEnergyBufferSize();
        final float barRatio = (float)Math.floor(ENERGY_BAR_WIDTH * energyPercent);
        final int minBar = tileEntity.getEnergy() > 0 ? 1 : 0;
        int renderWidth = (int)Math.min(Math.max(minBar, barRatio), ENERGY_BAR_WIDTH);
        this.drawTexturedModalRect(guiLeft + 141, guiTop + 66, 256 - ENERGY_BAR_WIDTH, 0, renderWidth, ENERGY_BAR_HEIGHT);
    }

    protected void updateTargetText() {
        if (this.tileEntity.getTarget() == null)
        {
            final BlockPos pos = this.tileEntity.getPos();
            final String targetText = String.format("%s, %s, %s", pos.getX(), pos.getY(), pos.getZ());
            this.fieldTarget.setText(targetText);
        }
        else
        {
            final Pos pos = this.tileEntity.getTarget();
            final String targetText = String.format("%s, %s, %s", pos.xi(), pos.yi(), pos.zi());
            this.fieldTarget.setText(targetText);
        }
    }



    protected void storeTargetText() {
        final String inputText = this.fieldTarget.getText();
        if(inputText != null) {
            final String[] split = inputText.split(",");
            if(split.length == 3) {
                try {
                    final int x = parseInt(split[0].trim());
                    final int y = parseInt(split[1].trim());
                    final int z = parseInt(split[2].trim());
                    final Pos newTarget = new Pos(x, y, z);
                    this.tileEntity.setTarget(newTarget);
                }
                catch (NumberFormatException e) {
                    targetError = ERROR_FORMAT_TARGET;
                }
            }
            else {
                targetError = ERROR_FORMAT_TARGET;
            }
        }
        else {
            targetError = ERROR_NULL;
        }
    }

    protected void sendTargetPacket() {
        ICBMClassic.packetHandler.sendToServer(new PacketTile("target_C>S", TileCruiseLauncher.SET_TARGET_PACKET_ID, tileEntity).addData(tileEntity.getTarget().xi(), this.tileEntity.getTarget().yi(), this.tileEntity.getTarget().zi()));
    }

    protected void updateHzText() {
        this.fieldHz.setText(this.tileEntity.radioCap.getChannel());
    }

    protected void storeHzText() {
        this.tileEntity.radioCap.setChannel(this.fieldHz.getText());
    }

    protected void sendHzPacket() {
        ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileCruiseLauncher.SET_FREQUENCY_PACKET_ID, tileEntity).addData(this.tileEntity.radioCap.getChannel()));
    }
}
