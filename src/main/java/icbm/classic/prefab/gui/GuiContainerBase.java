package icbm.classic.prefab.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.button.GuiButtonBase;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class GuiContainerBase<T extends Container> extends ContainerScreen<T>
{
    public static final ResourceLocation COMPONENTS_TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_components.png");

    public String currentTooltipText = "";

    protected ArrayList<IGuiComponent> components = new ArrayList();


    /** Debug toogle to render text for the ID and inventory ID for a slot */
    public boolean renderSlotDebugIDs = false;

    public GuiContainerBase(T container, PlayerInventory inv, ITextComponent titleIn)
    {
        super(container, inv, titleIn);
    }

    public abstract ResourceLocation getBackground();

    @Override
    public void init()
    {
        super.init();
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
    @Deprecated
    protected <E extends Button> E addButton(E button)
    {
        if(button instanceof IGuiComponent) {
            addComponent((IGuiComponent) button);
        }
        else {
            buttonList.add(button);
        }
        return button;
    }

    protected void drawString(String str, int x, int y, int color)
    {
        this.font.drawString(str, x, y, color);
    }

    protected void drawStringCentered(String str, int x, int y, int color)
    {
        drawString(str, x - (this.font.getStringWidth(str) / 2), y, color);
    }

    protected <T extends IGuiComponent> T addComponent(T field) {
        if(field instanceof Button) {
            buttonList.add((Button) field);
        }
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

        components.forEach(field -> {

            // Draw text
            field.drawForegroundLayer(mouseX, mouseY);

            // Detect if we need to display error feedback for the box
            if(field instanceof IToolTip && ((IToolTip) field).isWithin(mouseX, mouseY)) {
                final ITextComponent tooltip = ((IToolTip) field).getTooltip();
                if(tooltip != null) {
                    try {
                        this.currentTooltipText = LanguageUtility.buildToolTipString(tooltip);
                    }
                    catch (Exception e) {
                        if(ICBMClassic.runningAsDev) {
                            ICBMClassic.logger().error("Failed to format text for display", e);
                        }
                    }
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY); //TODO consider render tooltips in this step
        components.forEach(component -> component.draw(mouseX, mouseY, partialTicks));
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
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        drawDefaultBackground();

        this.mc.renderEngine.bindTexture(this.getBackground());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        components.forEach(component -> component.drawBackgroundLayer(partialTicks, mouseX, mouseY));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        components.forEach(component -> {
            if(!(component instanceof Button)) {
                component.onMouseClick(mouseX, mouseY, mouseButton);
            }
        });
    }

    @Override
    protected void actionPerformed(Button button) throws IOException
    {
        if(button instanceof GuiButtonBase) {
            ((GuiButtonBase) button).triggerAction();
        }
    }

    //TODO update and docs
    public void drawTooltip(int x, int y, Collection<String> toolTips)
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
            this.blit(backgroundX - 3, backgroundY - 4, backgroundX + textMaxWidth + 3, backgroundY - 3, var10, var10);
            this.blit(backgroundX - 3, backgroundY + var9 + 3, backgroundX + textMaxWidth + 3, backgroundY + var9 + 4, var10, var10);
            this.blit(backgroundX - 3, backgroundY - 3, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3, var10, var10);
            this.blit(backgroundX - 4, backgroundY - 3, backgroundX - 3, backgroundY + var9 + 3, var10, var10);
            this.blit(backgroundX + textMaxWidth + 3, backgroundY - 3, backgroundX + textMaxWidth + 4, backgroundY + var9 + 3, var10, var10);
            int var11 = 1347420415;
            int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
            this.blit(backgroundX - 3, backgroundY - 3 + 1, backgroundX - 3 + 1, backgroundY + var9 + 3 - 1, var11, var12);
            this.blit(backgroundX + textMaxWidth + 2, backgroundY - 3 + 1, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3 - 1, var11, var12);
            this.blit(backgroundX - 3, backgroundY - 3, backgroundX + textMaxWidth + 3, backgroundY - 3 + 1, var11, var11);
            this.blit(backgroundX - 3, backgroundY + var9 + 2, backgroundX + textMaxWidth + 3, backgroundY + var9 + 3, var12, var12);

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
