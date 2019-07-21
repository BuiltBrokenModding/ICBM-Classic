package icbm.classic.prefab.gui;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.lib.transform.vector.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class GuiContainerBase extends GuiContainer
{
    public static final ResourceLocation GUI_MC_BASE = new ResourceLocation(ICBMClassic.DOMAIN, ICBMClassic.GUI_DIRECTORY + "mc_base_empty.png");
    public static final ResourceLocation GUI_COMPONENTS = new ResourceLocation(ICBMClassic.DOMAIN, ICBMClassic.GUI_DIRECTORY + "gui_components.png");

    public ResourceLocation baseTexture;

    public String tooltip = "";

    protected HashMap<Rectangle, String> tooltips = new HashMap();
    protected ArrayList<GuiTextField> fields = new ArrayList();

    protected int containerWidth;
    protected int containerHeight;

    /** Debug toogle to render text for the ID and inventory ID for a slot */
    public boolean renderSlotDebugIDs = false;

    public GuiContainerBase(Container container)
    {
        super(container);
        this.baseTexture = GUI_MC_BASE;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonList.clear();
        this.fields.clear();
        this.tooltips.clear();
    }

    /**
     * Adds a button to the GUI
     *
     * @param button
     * @param <E>
     * @return
     */
    protected <E extends GuiButton> E addButton(E button)
    {
        buttonList.add(button);
        return button;
    }

    protected void drawString(String str, int x, int y, int color)
    {
        Minecraft.getMinecraft().fontRenderer.drawString(str, x, y, color);
    }

    protected void drawString(String str, int x, int y)
    {
        drawString(str, x, y, 4210752);
    }

    protected void drawString(String str, int x, int y, Color color)
    {
        drawString(str, x, y, color.getRGB());
    }

    protected void drawStringCentered(String str, int x, int y)
    {
        drawStringCentered(str, x, y, 4210752);
    }

    protected void drawStringCentered(String str, int x, int y, Color color)
    {
        drawStringCentered(str, x, y, color.getRGB());
    }

    protected void drawStringCentered(String str, int x, int y, int color)
    {
        drawString(str, x - (Minecraft.getMinecraft().fontRenderer.getStringWidth(str) / 2), y, color);
    }

    protected GuiTextField newField(int id, int x, int y, int w, String msg)
    {
        return this.newField(id, x, y, w, 20, msg);
    }

    protected GuiTextField newField(int id, int x, int y, int w, int h, String msg)
    {
        GuiTextField x_field = new GuiTextField(id, Minecraft.getMinecraft().fontRenderer, x, y, w, h);
        x_field.setText("" + msg);
        x_field.setMaxStringLength(15);
        x_field.setTextColor(16777215);
        fields.add(x_field);
        return x_field;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        super.handleMouseInput();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        for (Entry<Rectangle, String> entry : this.tooltips.entrySet())
        {
            if (entry.getKey().isWithin(new Point(mouseX - this.guiLeft, mouseY - this.guiTop)))
            {
                this.tooltip = entry.getValue();
                break;
            }
        }

        if (this.tooltip != null && this.tooltip != "")
        {
            java.util.List<String> lines = LanguageUtility.splitByLine(tooltip, LanguageUtility.toolTipLineLength);
            this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, lines.toArray(new String[lines.size()])); //TODO find a way to not have to convert to array to improve render time
        }

        this.tooltip = "";
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        renderHoveredToolTip(p_73863_1_, p_73863_2_);
        if (fields != null && fields.size() > 0)
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            for (GuiTextField field : fields)
            {
                field.drawTextBox();
            }
        }
    }

    @Override
    protected void keyTyped(char c, int id) throws IOException
    {
        //Key for debug render
        if (id == Keyboard.KEY_INSERT)
        {
            renderSlotDebugIDs = !renderSlotDebugIDs;
        }
        else
        {
            boolean f = false;
            for (GuiTextField field : fields)
            {
                field.textboxKeyTyped(c, id);
                if (field.isFocused())
                {
                    return;
                }
            }
            if (!f)
            {
                super.keyTyped(c, id);
            }
        }
    }

    @Override
    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException
    {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        for (GuiTextField field : fields)
        {
            field.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        drawDefaultBackground();
        this.containerWidth = (this.width - this.xSize) / 2;
        this.containerHeight = (this.height - this.ySize) / 2;

        this.mc.renderEngine.bindTexture(this.baseTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawTexturedModalRect(this.containerWidth, this.containerHeight, 0, 0, this.xSize, this.ySize);
    }

    //TODO update and docs
    protected void drawSlot(Slot slot)
    {
        drawSlot(slot.xPos - 1, slot.yPos - 1); //TODO get slot type from slot
        if (ICBMClassic.runningAsDev && renderSlotDebugIDs)
        {
            this.drawStringCentered("" + slot.getSlotIndex(), guiLeft + slot.xPos + 9, guiTop + slot.yPos + 9, Color.YELLOW);
            this.drawStringCentered("" + slot.slotNumber, guiLeft + slot.xPos + 9, guiTop + slot.yPos + 1, Color.RED);
        }
    }

    //TODO update and docs
    protected void drawSlot(int x, int y)
    {
        this.drawSlot(x, y, 1, 1, 1);
    }

    //TODO update and docs
    protected void drawSlot(int x, int y, float r, float g, float b)
    {
        this.mc.renderEngine.bindTexture(GUI_COMPONENTS);
        GL11.glColor4f(r, g, b, 1.0F);

        this.drawTexturedModalRect(this.containerWidth + x, this.containerHeight + y, 0, 0, 18, 18);
    }

    /**
     * Draws a rectangle with an increased or decreased width value
     * <p>
     * This works by duplicating the middle (3, width - 3) of the rectangle
     *
     * @param x        - render pos
     * @param y        - render pos
     * @param u        - x pos of the texture in it's texture sheet
     * @param v        - y pos of the texture in it's texture sheet
     * @param width    - width of the texture
     * @param height   - height of the texture
     * @param newWidth - new width to render the rectangle, minimal size of 6
     */
    protected void drawRectWithScaledWidth(int x, int y, int u, int v, int width, int height, int newWidth)
    {
        if (width > 0)
        {
            //If both widths are the same redirect to original call
            if (newWidth <= 0 || width == newWidth)
            {
                drawTexturedModalRect(x, y, u, v, width, height);
            }

            //Size of the middle section of the image
            final int midWidth = width - 6;

            //Start cap of image rect
            drawTexturedModalRect(x, y, u, v, 3, height);
            x += 3;

            //only render middle if it is larger than 6
            if (newWidth > 6)
            {
                //Loop over number of sections that need to be rendered
                int loops = newWidth / width;
                while (loops > 0)
                {
                    drawTexturedModalRect(x, y, u + 3, v, midWidth, height);
                    x += midWidth;
                    loops -= 1;
                }

                //Check if there is a remainder that still needs rendered
                loops = newWidth % width;
                if (loops != 0)
                {
                    drawTexturedModalRect(x, y, u + 3, v, loops, height);
                    x += loops;
                }
            }

            if (width > 3)
            {
                //End cap of image rect
                drawTexturedModalRect(x, y, u + width - 3, v, 3, height);
            }
        }
    }

    /**
     * Sets the render color for the GUI render
     *
     * @param color - color, null will force default
     */
    protected void setColor(Color color)
    {
        if (color == null)
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            GL11.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        }
    }

    //TODO update and docs
    public void drawTooltip(int x, int y, String... toolTips)
    {
        if (!GuiScreen.isShiftKeyDown())
        {
            if (toolTips != null)
            {
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                int var5 = 0;
                int var6;
                int var7;

                for (var6 = 0; var6 < toolTips.length; ++var6)
                {
                    var7 = Minecraft.getMinecraft().fontRenderer.getStringWidth(toolTips[var6]);

                    if (var7 > var5)
                    {
                        var5 = var7;
                    }
                }

                var6 = x + 12;
                var7 = y - 12;

                int var9 = 8;

                if (toolTips.length > 1)
                {
                    var9 += 2 + (toolTips.length - 1) * 10;
                }

                if (this.guiTop + var7 + var9 + 6 > this.height)
                {
                    var7 = this.height - var9 - this.guiTop - 6;
                }

                this.zLevel = 300;
                int var10 = -267386864;
                this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
                this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
                this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
                this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
                this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
                int var11 = 1347420415;
                int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
                this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
                this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
                this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
                this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

                for (int var13 = 0; var13 < toolTips.length; ++var13)
                {
                    String var14 = toolTips[var13];

                    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(var14, var6, var7, -1);
                    var7 += 10;
                }

                this.zLevel = 0;

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            }
        }
    }
}
