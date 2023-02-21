package icbm.classic.prefab.gui;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.lib.transform.vector.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class GuiContainerBase extends GuiContainer
{
    public static final ResourceLocation GUI_MC_BASE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "mc_base_empty.png");
    public static final ResourceLocation GUI_COMPONENTS = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_components.png");

    public ResourceLocation baseTexture;

    public String currentTooltipText = "";

    protected HashMap<Rectangle, Supplier<String>> tooltips = new HashMap();
    protected ArrayList<GuiTextField> fields = new ArrayList();


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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // Figure out which tooltip to render
        for (Entry<Rectangle, Supplier<String>> entry : this.tooltips.entrySet())
        {
            // First box with mouse wins
            if (entry.getKey().isWithin(new Point(mouseX - this.guiLeft, mouseY - this.guiTop)))
            {
                this.currentTooltipText = entry.getValue().get();
                break;
            }
        }

        // Render current tooltip if not empty
        if (!StringUtils.isEmpty(this.currentTooltipText))
        {
            java.util.List<String> lines = LanguageUtility.splitByLine(currentTooltipText);
            this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, lines);
        }

        // Reset tooltip for next render tick
        this.currentTooltipText = "";
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        renderHoveredToolTip(p_73863_1_, p_73863_2_); //TODO consider render tooltips in this step
        if (fields != null && fields.size() > 0)
        {
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
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

        this.mc.renderEngine.bindTexture(this.baseTexture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
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
        GlStateManager.color(r, g, b, 1.0F);

        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 0, 0, 18, 18);
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
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        }
    }

    //TODO update and docs
    public void drawTooltip(int x, int y, Collection<String> toolTips)
    {
        if (!GuiScreen.isShiftKeyDown())
        {
            if (toolTips != null)
            {
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableDepth();

                int textMaxWidth = 0;

                // Render all my lines
                for (String line : toolTips)
                {
                    final int lineWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(line);

                    // Tack longest line
                    if (lineWidth > textMaxWidth)
                    {
                        textMaxWidth = lineWidth;
                    }
                }

                int backgroundX = x + 12;
                int backgroundY = y - 12;

                int var9 = 8;

                if (toolTips.size() > 1)
                {
                    var9 += 2 + (toolTips.size()- 1) * 10;
                }

                if (this.guiTop + backgroundY + var9 + 6 > this.height)
                {
                    backgroundY = this.height - var9 - this.guiTop - 6;
                }

                this.zLevel = 300;
                int var10 = -267386864;
                this.drawGradientRect(backgroundX - 3, backgroundY - 4, backgroundX + textMaxWidth + 3, backgroundY - 3, var10, var10);
                this.drawGradientRect(backgroundX - 3, backgroundY + var9 + 3, backgroundX + textMaxWidth + 3, backgroundY + var9 + 4, var10, var10);
                this.drawGradientRect(backgroundX - 3, backgroundY - 3, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3, var10, var10);
                this.drawGradientRect(backgroundX - 4, backgroundY - 3, backgroundX - 3, backgroundY + var9 + 3, var10, var10);
                this.drawGradientRect(backgroundX + textMaxWidth + 3, backgroundY - 3, backgroundX + textMaxWidth + 4, backgroundY + var9 + 3, var10, var10);
                int var11 = 1347420415;
                int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
                this.drawGradientRect(backgroundX - 3, backgroundY - 3 + 1, backgroundX - 3 + 1, backgroundY + var9 + 3 - 1, var11, var12);
                this.drawGradientRect(backgroundX + textMaxWidth + 2, backgroundY - 3 + 1, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3 - 1, var11, var12);
                this.drawGradientRect(backgroundX - 3, backgroundY - 3, backgroundX + textMaxWidth + 3, backgroundY - 3 + 1, var11, var11);
                this.drawGradientRect(backgroundX - 3, backgroundY + var9 + 2, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3, var12, var12);

                // Draw text shadows
                for (String line : toolTips)
                {
                    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(line, backgroundX, backgroundY, -1);
                    backgroundY += 10;
                }

                this.zLevel = 0;

                GlStateManager.enableDepth();
                GlStateManager.enableRescaleNormal();
            }
        }
    }
}
