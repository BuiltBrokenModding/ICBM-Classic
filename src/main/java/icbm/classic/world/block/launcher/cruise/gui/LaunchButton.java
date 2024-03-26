package icbm.classic.world.block.launcher.cruise.gui;

import icbm.classic.prefab.gui.button.GuiButtonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Supplier;

public class LaunchButton extends GuiButtonBase<LaunchButton> {

    private boolean wasPressed;
    private boolean doDrawGlass = false;
    private Supplier<Boolean> enabledCheck;

    public LaunchButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 28, 29, "");
    }

    public LaunchButton setEnabledCheck(Supplier<Boolean> supplier) {
        this.enabledCheck = supplier;
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (enabledCheck != null) {
            this.enabled = enabledCheck.get();
        }
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
            if (!this.enabled) {
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

            // Draw button cover
            if (!enabled && doDrawGlass) {
                this.drawTexturedModalRect(this.x - 4, this.y - 4, 220, 5, 36, 36);
            }

            // Not sure why this is here
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    public LaunchButton doDrawDisabledGlass() {
        this.doDrawGlass = true;
        return this;
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
