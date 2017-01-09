package icbm.classic.client.gui;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.region.Rectangle;
import com.builtbroken.mc.lib.transform.vector.Point;
import com.mffs.api.utils.UnitDisplay;
import cpw.mods.fml.client.FMLClientHandler;
import icbm.classic.Reference;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.gui.GuiICBM;
import icbm.classic.content.machines.TileRadarStation;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiRadarStation extends GuiICBM
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.GUI_PATH + "gui_radar.png");
    public static final ResourceLocation TEXTURE_RED_DOT = new ResourceLocation(Reference.DOMAIN, Reference.GUI_PATH + "reddot.png");
    public static final ResourceLocation TEXTURE_YELLOW_DOT = new ResourceLocation(Reference.DOMAIN, Reference.GUI_PATH + "yellowdot.png");
    public static final ResourceLocation TEXTURE_WHITE_DOT = new ResourceLocation(Reference.DOMAIN, Reference.GUI_PATH + "whitedot.png");
    private TileRadarStation tileEntity;

    private int containerPosX;
    private int containerPosY;

    private GuiTextField textFieldAlarmRange;
    private GuiTextField textFieldSafetyZone;
    private GuiTextField textFieldFrequency;

    private List<Point> missileCoords = new ArrayList<Point>();

    private Point mouseOverCoords = new Point();
    private Point mousePosition = new Point();

    // Radar Map
    private Point radarCenter;
    private float radarMapRadius;

    private String info = "";

    private String info2;

    public GuiRadarStation(TileRadarStation tileEntity)
    {
        this.tileEntity = tileEntity;
        mouseOverCoords = new Point(this.tileEntity.xCoord, this.tileEntity.zCoord);
        ySize = 166;
        xSize = 256;
        radarCenter = new Point(this.containerPosX + this.xSize / 3 - 14, this.containerPosY + this.ySize / 2 + 4);
        radarMapRadius = TileRadarStation.MAX_DETECTION_RANGE / 63.8F;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.textFieldSafetyZone = new GuiTextField(fontRendererObj, 210, 67, 30, 12);
        this.textFieldSafetyZone.setMaxStringLength(3);
        this.textFieldSafetyZone.setText(this.tileEntity.safetyRange + "");

        this.textFieldAlarmRange = new GuiTextField(fontRendererObj, 210, 82, 30, 12);
        this.textFieldAlarmRange.setMaxStringLength(3);
        this.textFieldAlarmRange.setText(this.tileEntity.alarmRange + "");

        this.textFieldFrequency = new GuiTextField(fontRendererObj, 155, 112, 50, 12);
        this.textFieldFrequency.setMaxStringLength(6);
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
        this.fontRendererObj.drawString("\u00a77" + LanguageUtility.getLocal("icbm.machine.9.name"), this.xSize / 2 - 30, 6, 4210752);

        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.radar.coords"), 155, 18, 4210752);
        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.misc.x") + " " + (int) Math.round(mouseOverCoords.x()) + " " + LanguageUtility.getLocal("gui.misc.z") + " " + (int) Math.round(mouseOverCoords.y()), 155, 30, 4210752);

        this.fontRendererObj.drawString("\u00a76" + this.info, 155, 42, 4210752);
        this.fontRendererObj.drawString("\u00a74" + this.info2, 155, 54, 4210752);

        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.radar.zoneSafe"), 152, 70, 4210752);
        this.textFieldSafetyZone.drawTextBox();
        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.radar.zoneAlarm"), 150, 85, 4210752);
        this.textFieldAlarmRange.drawTextBox();

        this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.misc.freq"), 155, 100, 4210752);
        this.textFieldFrequency.drawTextBox();

        this.fontRendererObj.drawString(UnitDisplay.getDisplay(TileRadarStation.WATTS, UnitDisplay.Unit.WATT), 155, 128, 4210752);

        //this.fontRendererObj.drawString(UnitDisplay.getDisplay(this.tileEntity.getVoltageInput(null), Unit.VOLTAGE), 155, 138, 4210752);

        // Shows the status of the radar
        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");

        if (this.tileEntity.hasPower())
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.radar.on");
        }
        else
        {
            status = LanguageUtility.getLocal("gui.radar.nopower");
        }

        this.fontRendererObj.drawString(color + status, 155, 150, 4210752);
    }

    /** Call this method from you GuiScreen to process the keys into textbox. */
    @Override
    public void keyTyped(char par1, int par2)
    {
        super.keyTyped(par1, par2);
        this.textFieldSafetyZone.textboxKeyTyped(par1, par2);
        this.textFieldAlarmRange.textboxKeyTyped(par1, par2);
        this.textFieldFrequency.textboxKeyTyped(par1, par2);

        try
        {
            int newSafetyRadius = Math.min(TileRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldSafetyZone.getText())));
            this.tileEntity.safetyRange = newSafetyRadius;
            Engine.instance.packetHandler.sendToServer(new PacketTile(this.tileEntity, 2, this.tileEntity.safetyRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            int newAlarmRadius = Math.min(TileRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldAlarmRange.getText())));
            this.tileEntity.alarmRange = newAlarmRadius;
            Engine.instance.packetHandler.sendToServer(new PacketTile(this.tileEntity, 3, this.tileEntity.alarmRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            this.tileEntity.setFrequency(Integer.parseInt(this.textFieldFrequency.getText()));
            Engine.instance.packetHandler.sendToServer(new PacketTile(this.tileEntity, 4, this.tileEntity.getFrequency()));
        }
        catch (NumberFormatException e)
        {
        }

    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
        this.textFieldAlarmRange.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldSafetyZone.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldFrequency.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

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

            for (Entity entity : this.tileEntity.detectedEntities)
            {
                Point position = new Point(radarCenter.x() + (entity.posX - this.tileEntity.xCoord) / this.radarMapRadius, radarCenter.y() - (entity.posZ - this.tileEntity.zCoord) / this.radarMapRadius);

                if (entity instanceof EntityMissile)
                {
                    if (this.tileEntity.isMissileGoingToHit((EntityMissile) entity))
                    {
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_RED_DOT);
                    }
                    else
                    {
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_YELLOW_DOT);
                    }
                }
                else
                {
                    FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_YELLOW_DOT);
                }

                this.drawTexturedModalRect(position.xi(), position.yi(), 0, 0, 2, 2);

                // Hover Detection
                Point minPosition = position.clone();
                minPosition.add(-range);
                Point maxPosition = position.clone();
                maxPosition.add(range);

                if (new Rectangle(minPosition, maxPosition).isWithin(this.mousePosition))
                {
                    this.info = entity.getCommandSenderName();

                    if (entity instanceof EntityPlayer)
                    {
                        this.info = "\u00a71" + this.info;
                    }

                    if (entity instanceof EntityMissile)
                    {
                        if (((EntityMissile) entity).targetVector != null)
                        {
                            this.info2 = "(" + ((EntityMissile) entity).targetVector.xi() + ", " + ((EntityMissile) entity).targetVector.zi() + ")";
                        }
                    }
                }
            }
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

                    this.mouseOverCoords = new Point(this.tileEntity.xCoord + xBlockDistance, this.tileEntity.zCoord - yBlockDistance);
                }
            }
        }

        if (!this.textFieldSafetyZone.isFocused())
        {
            this.textFieldSafetyZone.setText(this.tileEntity.safetyRange + "");
        }
        if (!this.textFieldAlarmRange.isFocused())
        {
            this.textFieldAlarmRange.setText(this.tileEntity.alarmRange + "");
        }
    }
}
