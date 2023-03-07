package icbm.classic.content.blocks.emptower.gui;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import java.io.IOException;

public class GuiEMPTower extends GuiContainerBase
{
    // Localizations
    private static final String LANG_KEY = "gui.empTower";
    private static final String GUI_NAME = LANG_KEY + ".name";
    private static final String LANG_ERROR = LANG_KEY + ".error";
    private static final String ERROR_FORMAT_RANGE = LANG_ERROR + ".format.range";
    private static final String POWER_NEEDED = LANG_KEY + ".power";
    private static final String COOLING_NEEDED = LANG_KEY + ".cooling";
    private static final String READY = LANG_KEY + ".ready";

    //UV
    final int ENERGY_BAR_WIDTH = 16;
    final int ENERGY_BAR_HEIGHT = 2;

    // Texture
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_emp_tower.png");

    private final TileEMPTower tileEntity;
    private TextInput fieldRange;
    private TextInput fieldHz;
    private GuiButton firingButton;

    // User feedback
    private String targetError = null;
    private String hzError = null;

    public GuiEMPTower(EntityPlayer player, TileEMPTower tileEntity)
    {
        super(new ContainerEMPTower(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 166;
        this.xSize = 175;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        int componentID = 0;

        // Target field
        this.fieldRange = new TextInput(componentID++, fontRenderer, 18, 17, 40, 12);
        this.fieldRange.setFocusChangedCallback((state) -> {
            if(!state) {

                // Reset previous error state
                this.targetError = null;

                // Parse input from user and store into tile client side
                storeRangeText();

                // Don't send or store if we have broken input
                if(targetError == null) {
                    sendRangePacket();
                    updateRangeText();
                }

                // Enable error rendering
                this.fieldRange.isErrored(targetError != null);
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
        this.firingButton = addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38));
        this.tooltips.put(new Rectangle(24, 38, 24 + 28, 38 + 29), () -> {
            if(!tileEntity.isReady()) {
                if(tileEntity.getCooldown() > 0) {
                    return LanguageUtility.getLocal(COOLING_NEEDED).replace("%1$s", String.format("%.2f",tileEntity.getCooldownPercentage() * 100));
                }
                else if(!tileEntity.checkExtract()) {
                    return LanguageUtility.getLocal(POWER_NEEDED).replace("%1$s", String.format("%.2f",tileEntity.getChargePercentage() * 100));
                }
            }
            return LanguageUtility.getLocal(READY);
        });

        // Energy bar
        this.tooltips.put(new Rectangle(141, 66, 141 + ENERGY_BAR_WIDTH, 66 + ENERGY_BAR_HEIGHT), () -> String.format("%s / %s FE", tileEntity.getEnergy(), tileEntity.getEnergyBufferSize()));
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        this.fieldRange.textboxKeyTyped(par1, par2);
        this.fieldHz.textboxKeyTyped(par1, par2);
    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        this.fieldRange.mouseClicked(par1 - guiLeft, par2 - guiTop, par3);
        this.fieldHz.mouseClicked(par1 - guiLeft, par2 - guiTop, par3);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if(button.id == firingButton.id)
            ICBMClassic.packetHandler.sendToServer(new PacketTile("fire_C>S", TileEMPTower.FIRE_PACKET_ID, this.tileEntity));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // Draw text
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal(GUI_NAME), 52, 6, 4210752);
        this.fontRenderer.drawString("/ " + tileEntity.getMaxRadius(), 62, 19, 4210752);

        // Draw components
        this.fieldRange.drawTextBox();
        this.fieldHz.drawTextBox();

        // Draw button cover
        if(!firingButton.enabled) {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            // TODO add animation to opening glass
            this.drawTexturedModalRect(20, 35, 220, 5, 36, 36);
        }

        // Goes last so tooltips render above our UI elements
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        //Draw background
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

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        // Update from tile's data
        if(!fieldRange.isFocused())
            updateRangeText();
        if(!fieldHz.isFocused())
            updateHzText();

        // only enable button if tile is ready to launch
        this.firingButton.enabled = this.tileEntity.isReady();
    }

    protected void updateRangeText() {
        this.fieldRange.setText(Integer.toString(tileEntity.range));
    }



    protected void storeRangeText() {
        final String inputText = this.fieldRange.getText();
        try {
            this.tileEntity.range = Integer.parseInt(inputText);
        }
        catch (NumberFormatException e) {
            targetError = ERROR_FORMAT_RANGE;
        }
    }

    protected void sendRangePacket() {
        ICBMClassic.packetHandler.sendToServer(new PacketTile("range_C>S", TileEMPTower.CHANGE_RADIUS_PACKET_ID, tileEntity).addData(tileEntity.range));
    }

    protected void updateHzText() {
        this.fieldHz.setText(this.tileEntity.radioCap.getChannel());
    }

    protected void storeHzText() {
        this.tileEntity.radioCap.setChannel(this.fieldHz.getText());
    }

    protected void sendHzPacket() {
        ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileEMPTower.CHANGE_HZ_PACKET_ID, tileEntity).addData(this.tileEntity.radioCap.getChannel()));
    }
}
