package icbm.classic.content.blocks.launcher.cruise.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class LaunchButton extends GuiButton {

    protected boolean wasPressed;

    public LaunchButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 28, 29, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            // Set color and texture
            mc.getTextureManager().bindTexture(GuiCruiseLauncher.TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Check if mouse is over button TODO check for circle
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;


            final int UV_WIDTH = 182;

            // Disabled state
            if(!this.enabled) {
                this.drawTexturedModalRect(this.x, this.y, UV_WIDTH, 111, this.width, this.height);
            }
            // Pressed state
            else if (this.wasPressed) {
                this.drawTexturedModalRect(this.x, this.y, UV_WIDTH, 76, this.width, this.height);
            }
            // Hover state
            else if (this.hovered) {
                this.drawTexturedModalRect(this.x, this.y, UV_WIDTH, 41, this.width, this.height);
            }
            // Default state
            else {
                this.drawTexturedModalRect(this.x, this.y, UV_WIDTH, 6, this.width, this.height);
            }

            // Not sure why this is here
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.wasPressed = false;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.wasPressed = true;
            return true;
        }
        return false;
    }
}
