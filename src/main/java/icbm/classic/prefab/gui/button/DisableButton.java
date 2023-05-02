package icbm.classic.prefab.gui.button;

import icbm.classic.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class DisableButton extends GuiButtonBase<DisableButton> implements IGuiComponent, IToolTip {

    // Icon this designed to cover is 13x13
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;

    private static final int UV_Y = 241;
    private static final int UV_X = 0;

    /** Checks if disable status should render, not the same as #enabled or #visble. Even though this causes it to stop rendering is true */
    private final Supplier<Boolean> shouldShowAsDisabled;
    public DisableButton(int buttonId, int x, int y, Supplier<Boolean> shouldShowAsDisabled) {
        super(buttonId, x, y, WIDTH, HEIGHT, "");
        this.shouldShowAsDisabled = shouldShowAsDisabled;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            // Set color and texture
            mc.getTextureManager().bindTexture(GuiContainerBase.COMPONENTS_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Check if mouse is over button TODO check for circle
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            //TODO add disabled state

            // Hover state
            if (this.hovered && GuiContainer.isAltKeyDown()) {
                this.drawTexturedModalRect(this.x, this.y, UV_X + WIDTH, UV_Y, this.width, this.height);
            }
            // Default state
            else if(shouldShowAsDisabled.get()) {
                this.drawTexturedModalRect(this.x, this.y, UV_X, UV_Y, this.width, this.height);
            }

            // Not sure why this is here
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return super.mousePressed(mc, mouseX, mouseY) && GuiContainer.isAltKeyDown();
    }
}
