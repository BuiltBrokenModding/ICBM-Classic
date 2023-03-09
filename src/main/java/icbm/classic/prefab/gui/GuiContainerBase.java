package icbm.classic.prefab.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.button.GuiButtonBase;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

public abstract class GuiContainerBase extends GuiContainer
{
    public String currentTooltipText = "";

    protected ArrayList<IGuiComponent> components = new ArrayList();


    /** Debug toogle to render text for the ID and inventory ID for a slot */
    public boolean renderSlotDebugIDs = false;

    public GuiContainerBase(Container container)
    {
        super(container);
    }

    protected abstract ResourceLocation getBackground();

    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonList.clear();
        this.components.clear();
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
        if(button instanceof IGuiComponent) {
            addComponent((IGuiComponent) button);
        }
        buttonList.add(button);
        return button;
    }

    protected void drawString(String str, int x, int y, int color)
    {
        Minecraft.getMinecraft().fontRenderer.drawString(str, x, y, color);
    }

    protected void drawStringCentered(String str, int x, int y, int color)
    {
        drawString(str, x - (Minecraft.getMinecraft().fontRenderer.getStringWidth(str) / 2), y, color);
    }

    protected <T extends IGuiComponent> T addComponent(T field) {
        components.add(field);
        field.onAddedToHost(this);
        return field;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        components.forEach(IGuiComponent::onUpdate);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        final int cursorX = mouseX - this.guiLeft;
        final int cursorY = mouseY - this.guiTop;

        components.forEach(field -> {

            // Draw text
            field.drawForegroundLayer(mouseX, mouseY);

            // Detect if we need to display error feedback for the box
            if(field instanceof IToolTip && ((IToolTip) field).isWithin(cursorX, cursorY)) {
                final String tooltip = ((IToolTip) field).getTooltip();
                if(!StringUtils.isEmpty(tooltip)) {
                    this.currentTooltipText = LanguageUtility.getLocal(tooltip);
                }
            }
        });

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
            boolean f = components.stream().anyMatch(component -> component.onKeyTyped(c, id));
            if (!f)
            {
                super.keyTyped(c, id);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        drawDefaultBackground();

        this.mc.renderEngine.bindTexture(this.getBackground());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        final int cursorX = mouseX - this.guiLeft;
        final int cursorY = mouseY - this.guiTop;
        components.forEach(component -> component.drawBackgroundLayer(f, cursorX, cursorY));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        final int cursorX = mouseX - this.guiLeft;
        final int cursorY = mouseY - this.guiTop;
        components.forEach(component -> {
            if(!(component instanceof GuiButton)) {
                component.onMouseClick(cursorX, cursorY, mouseButton);
            }
        });
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button instanceof GuiButtonBase) {
            ((GuiButtonBase) button).triggerAction();
        }
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
