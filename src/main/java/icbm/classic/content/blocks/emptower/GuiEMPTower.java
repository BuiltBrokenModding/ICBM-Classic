package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import java.io.IOException;

public class GuiEMPTower extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_empty.png");

    private TileEMPTower tileEntity;
    private GuiTextField textFieldBanJing;

    private int containerWidth;
    private int containerHeight;

    public GuiEMPTower(EntityPlayer player, TileEMPTower tileEntity)
    {
        super(new ContainerEMPTower(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 166;
    }

    /** Adds the buttons (and other controls) to the screen in question. */
    @Override
    public void initGui()
    {
        super.initGui();

        this.buttonList.add(new GuiButton(0, this.width / 2 - 77, this.height / 2 - 10, 50, 20, LanguageUtility.getLocal("gui.empTower.missiles")));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 25, this.height / 2 - 10, 65, 20, LanguageUtility.getLocal("gui.empTower.elec")));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 43, this.height / 2 - 10, 35, 20, LanguageUtility.getLocal("gui.empTower.both")));

        this.textFieldBanJing = new GuiTextField(0, fontRenderer, 72, 28, 30, 12);
        this.textFieldBanJing.setMaxStringLength(3);
        this.textFieldBanJing.setText(this.tileEntity.empRadius + "");
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        switch (par1GuiButton.id)
        {
            case 0:
                this.tileEntity.empMode = EMPMode.MISSILES_ONLY;
                break;
            case 1:
                this.tileEntity.empMode = EMPMode.ELECTRICITY_ONLY;
                break;
            case 2:
                this.tileEntity.empMode = EMPMode.ALL;
                break;
        }

        ICBMClassic.packetHandler.sendToServer(new PacketTile("mode_C>S", TileEMPTower.CHANGE_MODE_PACKET_ID, this.tileEntity).addData((byte)this.tileEntity.empMode.ordinal()));
    }

    /** Call this method from you GuiScreen to process the keys into textbox. */
    @Override
    public void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        this.textFieldBanJing.textboxKeyTyped(par1, par2);

        try
        {
            int radius = Math.min(Math.max(Integer.parseInt(this.textFieldBanJing.getText()), 10), TileEMPTower.MAX_RADIUS);
            this.tileEntity.empRadius = radius;
            ICBMClassic.packetHandler.sendToServer(new PacketTile("range_C>S", TileEMPTower.CHANGE_RADIUS_PACKET_ID, this.tileEntity).addData(this.tileEntity.empRadius));
        }
        catch (NumberFormatException e)
        {

        }
    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int x, int y, int par3) throws IOException
    {
        super.mouseClicked(x, y, par3);
        this.textFieldBanJing.mouseClicked(x - containerWidth, y - containerHeight, par3);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("gui.empTower.name"), 65, 6, 4210752);

        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.empTower.radius").replaceAll("%p", "        "), 12, 30, 4210752);
        this.textFieldBanJing.drawTextBox();

        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.empTower.effect"), 12, 55, 4210752);

        // Shows the EMP mode of the EMP Tower
        String mode = LanguageUtility.getLocal("gui.empTower.effectDebilitate");

        if (this.tileEntity.empMode == EMPMode.MISSILES_ONLY)
        {
            mode = LanguageUtility.getLocal("gui.empTower.effectDisrupt");
        }
        else if (this.tileEntity.empMode == EMPMode.ELECTRICITY_ONLY)
        {
            mode = LanguageUtility.getLocal("gui.empTower.effectDeplete");
        }

        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.empTower.mode") + " " + mode, 12, 105, 4210752);

        // Shows the status of the EMP Tower
        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");

        if (!this.tileEntity.hasPower())
        {
            status = LanguageUtility.getLocal("gui.misc.nopower");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.empTower.ready");
        }

        this.fontRenderer.drawString(color + LanguageUtility.getLocal("gui.misc.status") + " " + status, 12, 120, 4210752);
        this.fontRenderer.drawString("Energy: " + this.tileEntity.getEnergy() + "/" + this.tileEntity.getEnergyBufferSize(), 12, 150, 4210752);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        int containerPosX = (this.width - this.xSize) / 2;
        int containerPosY = (this.height - this.ySize) / 2;

        //Draw background
        drawDefaultBackground();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        containerWidth = (this.width - this.xSize) / 2;
        containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);

        //Draw energy bar
        if (tileEntity.getEnergy() > 0)
        {
            float energyScale = tileEntity.getEnergy() / (float) tileEntity.getEnergyBufferSize();

            final int textureX = 352 / 2;
            final int textureWidth = 8;
            final int textureHeight = 142 / 2;
            final int height = (int) Math.min(textureHeight, Math.floor(textureHeight * energyScale));
            this.drawTexturedModalRect(containerPosX + 168, containerPosY + 65 + (textureHeight - height), textureX, textureHeight - height, textureWidth, height);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (!this.textFieldBanJing.isFocused())
        {
            this.textFieldBanJing.setText(this.tileEntity.empRadius + "");
        }
    }
}
