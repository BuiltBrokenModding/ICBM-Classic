package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.lib.transform.vector.Point;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Mouse;
import java.io.IOException;

public class GuiRadarStation extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_radar.png");
    public static final ResourceLocation TEXTURE_RED_DOT = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "reddot.png");
    public static final ResourceLocation TEXTURE_YELLOW_DOT = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "yellowdot.png");
    public static final ResourceLocation TEXTURE_WHITE_DOT = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "whitedot.png");
    private TileRadarStation tileEntity;

    private int containerPosX;
    private int containerPosY;

    private GuiTextField textFieldDetectionRange;
    private GuiTextField textFieldTriggerRange;
    private GuiTextField textFieldFrequency;

    private Point mouseOverCoords = new Point();
    private Point mousePosition = new Point();

    // Radar Map
    private Point radarCenter;
    private float radarMapRadius;

    private String info = "";

    private String info2;

    public GuiRadarStation(EntityPlayer player, TileRadarStation tileEntity)
    {
        super(new ContainerRadarStation(player, tileEntity));
        this.tileEntity = tileEntity;
        mouseOverCoords = new Point(this.tileEntity.getPos().getX(), this.tileEntity.getPos().getZ());
        ySize = 166;
        xSize = 256;
        radarCenter = new Point(this.containerPosX + this.xSize / 3 - 14, this.containerPosY + this.ySize / 2 + 4);
        radarMapRadius = TileRadarStation.MAX_DETECTION_RANGE / 63.8F;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.textFieldTriggerRange = new GuiTextField(0, fontRenderer, 210, 67, 30, 12);
        this.textFieldTriggerRange.setMaxStringLength(3);
        this.textFieldTriggerRange.setText(this.tileEntity.safetyRange + "");

        this.textFieldDetectionRange = new GuiTextField(1, fontRenderer, 210, 82, 30, 12);
        this.textFieldDetectionRange.setMaxStringLength(3);
        this.textFieldDetectionRange.setText(this.tileEntity.alarmRange + "");

        this.textFieldFrequency = new GuiTextField(2, fontRenderer, 210, 108, 30, 12);
        this.textFieldFrequency.setMaxStringLength(3);
        this.textFieldFrequency.setText(this.tileEntity.getFrequency() + "");

        //Engine.instance.packetHandler.sendToServer(new PacketTile(this.tileEntity, -1, true));
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        //Engine.instance.packetHandler.sendToServer(new PacketTile(this.tileEntity, -1, false));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        //Header
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocalName(BlockReg.blockRadarStation.getTranslationKey()), this.xSize / 2 - 30, 6, 4210752);

        //Coredinates header
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.radar.coords"), 155, 18, 4210752);

        //Coordinates
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.x")
                + " " + (int) Math.floor(mouseOverCoords.x())
                + " " + LanguageUtility.getLocal("gui.misc.z")
                + " " + (int) Math.floor(mouseOverCoords.y()), 155, 30, 4210752);

        this.fontRenderer.drawString(this.info, 155, 42, 4210752);
        this.fontRenderer.drawString("\u00a74" + this.info2, 155, 54, 4210752);

        //Range Header
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.radar.range"), 152, 55, 4210752);

        //Trigger range
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.radar.range.trigger"), 155, 70, 4210752);
        this.textFieldTriggerRange.drawTextBox();

        //Detection range
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.radar.range.detection"), 155, 85, 4210752);
        this.textFieldDetectionRange.drawTextBox();

        //Hz
        this.fontRenderer.drawString(LanguageUtility.getLocal("gui.misc.freq"), 152, 110, 4210752);
        this.textFieldFrequency.drawTextBox();

        // Shows the status of the radar
        String color = "\u00a74";
        String status;

        if (this.tileEntity.hasPower())
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.radar.on");
        }
        else
        {
            status = LanguageUtility.getLocal("gui.radar.nopower");
        }

        this.fontRenderer.drawString(color + status, 155, 150, 4210752);
    }

    /** Call this method from you GuiScreen to process the keys into textbox. */
    @Override
    public void keyTyped(char par1, int par2) throws IOException
    {
        super.keyTyped(par1, par2);
        this.textFieldTriggerRange.textboxKeyTyped(par1, par2);
        this.textFieldDetectionRange.textboxKeyTyped(par1, par2);
        this.textFieldFrequency.textboxKeyTyped(par1, par2);

        try
        {
            int newSafetyRadius = Math.min(TileRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldTriggerRange.getText())));
            this.tileEntity.safetyRange = newSafetyRadius;
            ICBMClassic.packetHandler.sendToServer(new PacketTile("safeRange_C>S", TileRadarStation.SET_SAFETY_RANGE_PACKET_ID, this.tileEntity).addData(this.tileEntity.safetyRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            int newAlarmRadius = Math.min(TileRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldDetectionRange.getText())));
            this.tileEntity.alarmRange = newAlarmRadius;
            ICBMClassic.packetHandler.sendToServer(new PacketTile("alarmRange_C>S", TileRadarStation.SET_ALARM_RANGE_PACKET_ID, this.tileEntity).addData(this.tileEntity.alarmRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            this.tileEntity.setFrequency(Integer.parseInt(this.textFieldFrequency.getText()));
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileRadarStation.SET_FREQUENCY_PACKET_ID, this.tileEntity).addData(this.tileEntity.getFrequency()));
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
        this.textFieldDetectionRange.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldTriggerRange.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldFrequency.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        drawDefaultBackground();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.containerPosX = (this.width - this.xSize) / 2;
        this.containerPosY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerPosX, containerPosY, 0, 0, this.xSize, this.ySize);

        this.radarCenter = new Point(this.containerPosX + this.xSize / 3 - 10, this.containerPosY + this.ySize / 2 + 4);
        this.radarMapRadius = TileRadarStation.MAX_DETECTION_RANGE / 71f;

        this.info = "";
        this.info2 = "";

        if (this.tileEntity.hasPower())
        {
            int range = 4;

            for (Pos pos : tileEntity.guiDrawPoints)
            {
                final RadarObjectType type = RadarObjectType.get(pos.zi());

                final double x = pos.x();
                final double z = pos.y();

                Point position = new Point(radarCenter.x() + (x - this.tileEntity.getPos().getX()) / this.radarMapRadius,
                        radarCenter.y() - (z - this.tileEntity.getPos().getZ()) / this.radarMapRadius);

                switch (type)
                {
                    case MISSILE:
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_YELLOW_DOT);
                        break;
                    case MISSILE_IMPACT:
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_RED_DOT);
                        break;
                    case OTHER:
                    default:
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_WHITE_DOT);
                        break;
                }

                this.drawTexturedModalRect(position.xi(), position.yi(), 0, 0, 2, 2);

                // Hover Detection
                Point minPosition = position.add(-range);
                Point maxPosition = position.add(range);

                if (new Rectangle(minPosition, maxPosition).isWithin(this.mousePosition))
                {
                    if (type == RadarObjectType.OTHER)
                    {
                        this.info = String.format(LanguageUtility.getLocal("gui.misc.object"), (int)x, (int)z);
                    }
                    else
                    {
                        this.info = (type == RadarObjectType.MISSILE ? "\u00a76" : "\u00a74") + String.format(LanguageUtility.getLocal("gui.misc.missile"), (int)x, (int)z);
                    }
                }
            }
        }

        //Draw energy bar
        if (tileEntity.getEnergy() > 0)
        {
            float energyScale = tileEntity.getEnergy() / (float) tileEntity.getEnergyBufferSize();

            final int textureWidth = 8;
            final int textureHeight = 142 / 2;
            final int height = (int) Math.min(textureHeight, Math.floor(textureHeight * energyScale));
            this.drawTexturedModalRect(containerPosX + 248, containerPosY + 65 + (textureHeight - height), 0, (332 / 2) + textureHeight - height, textureWidth, height);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (Mouse.isInsideWindow())
        {
            if (Mouse.getEventButton() == -1)
            {
                this.mousePosition = new Point(Mouse.getEventX() * this.width / this.mc.displayWidth, this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1);

                float difference = TileRadarStation.MAX_DETECTION_RANGE / this.radarMapRadius;

                if (this.mousePosition.x() > this.radarCenter.x() - difference && this.mousePosition.x() < this.radarCenter.x() + difference && this.mousePosition.y() > this.radarCenter.y() - difference && this.mousePosition.y() < this.radarCenter.y() + difference)
                {
                    // Calculate from the mouse position the relative position
                    // on the grid
                    int xDifference = (int) (this.mousePosition.x() - this.radarCenter.x());
                    int yDifference = (int) (this.mousePosition.y() - this.radarCenter.y());
                    int xBlockDistance = (int) (xDifference * this.radarMapRadius);
                    int yBlockDistance = (int) (yDifference * this.radarMapRadius);

                    this.mouseOverCoords = new Point(this.tileEntity.getPos().getX() + xBlockDistance, this.tileEntity.getPos().getZ() - yBlockDistance);
                }
            }
        }

        if (!this.textFieldTriggerRange.isFocused())
        {
            this.textFieldTriggerRange.setText(this.tileEntity.safetyRange + "");
        }
        if (!this.textFieldDetectionRange.isFocused())
        {
            this.textFieldDetectionRange.setText(this.tileEntity.alarmRange + "");
        }
    }
}
