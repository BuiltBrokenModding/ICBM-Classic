package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import java.io.IOException;

import static java.lang.Integer.parseInt;

public class GuiCruiseLauncher extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_cruise_launcher.png");

    private TileCruiseLauncher tileEntity;
    private GuiTextField textFieldX;
    private GuiTextField textFieldZ;
    private GuiTextField textFieldY;
    private GuiTextField textFieldFreq;
    private GuiButton launch_button;

    public GuiCruiseLauncher(EntityPlayer player, TileCruiseLauncher tileEntity)
    {
        super(new ContainerCruiseLauncher(player, tileEntity));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.textFieldX = new GuiTextField(0, fontRenderer, 20, 21, 35, 12);
        this.textFieldY = new GuiTextField(1, fontRenderer, 20, 37, 35, 12);
        this.textFieldZ = new GuiTextField(2, fontRenderer, 20, 52, 35, 12);
        this.textFieldFreq = new GuiTextField(3,fontRenderer, 70, 33, 35, 12);
        this.textFieldFreq.setMaxStringLength(4);
        this.textFieldX.setMaxStringLength(6);
        this.textFieldZ.setMaxStringLength(6);
        this.textFieldY.setMaxStringLength(6);

        this.textFieldFreq.setText(this.tileEntity.getFrequency() + "");

        launch_button = addButton(new GuiButton(0, guiLeft + 69, guiTop + 60, 70, 20, LanguageUtility.getLocal("gui.launcherscreen.launch")));

        if (this.tileEntity.getTarget() == null)
        {
            this.textFieldX.setText(Math.round(this.tileEntity.getPos().getX()) + "");
            this.textFieldZ.setText(Math.round(this.tileEntity.getPos().getZ()) + "");
            this.textFieldY.setText(Math.round(this.tileEntity.getPos().getY()) + "");
        }
        else
        {
            this.textFieldX.setText(Math.round(this.tileEntity.getTarget().x()) + "");
            this.textFieldZ.setText(Math.round(this.tileEntity.getTarget().z()) + "");
            this.textFieldY.setText(Math.round(this.tileEntity.getTarget().y()) + "");
        }
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        this.textFieldX.textboxKeyTyped(par1, par2);
        this.textFieldZ.textboxKeyTyped(par1, par2);
        this.textFieldY.textboxKeyTyped(par1, par2);
        this.textFieldFreq.textboxKeyTyped(par1, par2);

        try
        {
            Pos newTarget = new Pos(parseInt(this.textFieldX.getText()), parseInt(this.textFieldY.getText()), parseInt(this.textFieldZ.getText()));
            this.tileEntity.setTarget(newTarget);
            ICBMClassic.packetHandler.sendToServer(new PacketTile("target_C>S", TileCruiseLauncher.SET_TARGET_PACKET_ID, tileEntity).addData(tileEntity.getTarget().xi(), this.tileEntity.getTarget().yi(), this.tileEntity.getTarget().zi()));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            short newFrequency = (short) Math.max(Short.parseShort(this.textFieldFreq.getText()), 0);
            this.tileEntity.setFrequency(newFrequency);
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileCruiseLauncher.SET_FREQUENCY_PACKET_ID, tileEntity).addData(tileEntity.getFrequency()));
        }
        catch (NumberFormatException e)
        {
        }
    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);
        this.textFieldX.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
        this.textFieldZ.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
        this.textFieldY.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
        this.textFieldFreq.mouseClicked(par1 - containerWidth, par2 - containerHeight, par3);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button.id == launch_button.id)
            ICBMClassic.packetHandler.sendToServer(new PacketTile("launch_C>S", TileCruiseLauncher.LAUNCH_PACKET_ID, this.tileEntity));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRenderer.drawString("\u00a77" + "Cruise Launcher", 52, 6, 4210752);

        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.x"), 8, 23, 4210752);
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.y"), 8, 39, 4210752);
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.z"), 8, 54, 4210752);

        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.freq"), 70, 20, 4210752);

        this.textFieldX.drawTextBox();
        this.textFieldZ.drawTextBox();
        this.textFieldY.drawTextBox();
        this.textFieldFreq.drawTextBox();

        this.fontRenderer.drawString(this.tileEntity.getStatus(), 70, 50, 4210752);
        //this.fontRenderer.drawString(this.tileEntity.getVoltageInput(null) + "v", 70, 60, 4210752);
        //this.fontRenderer.drawString(UnitDisplay.getDisplayShort(this.tileEntity.getEnergyHandler().getEnergy(), Unit.JOULES) + "/" + UnitDisplay.getDisplayShort(this.tileEntity.getEnergyHandler().getEnergyCapacity(), Unit.JOULES), 70, 70, 4210752);

        this.fontRenderer.drawString(LanguageUtility.getLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        drawDefaultBackground();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        containerWidth = (this.width - this.xSize) / 2;
        containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (!this.textFieldX.isFocused())
            this.textFieldX.setText(Math.round(this.tileEntity.getTarget().x()) + "");
        if (!this.textFieldZ.isFocused())
            this.textFieldZ.setText(Math.round(this.tileEntity.getTarget().z()) + "");
        if (!this.textFieldY.isFocused())
            this.textFieldY.setText(Math.round(this.tileEntity.getTarget().y()) + "");
        if (!this.textFieldFreq.isFocused())
            this.textFieldFreq.setText(this.tileEntity.getFrequency() + "");
    }
}
