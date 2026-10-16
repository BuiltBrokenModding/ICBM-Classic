package icbm.classic.content.blocks.launcher.cruise.gui;

import icbm.classic.prefab.gui.button.GuiButtonBase;
import net.minecraft.client.gui.GuiGraphics;

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
        if(enabledCheck != null) {
            this.active = enabledCheck.get();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Check if mouse is over button TODO check for circle
        final boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        final int UV_WIDTH = 182;

        // Disabled state
        if(!this.active) {
            graphics.blit(GuiCruiseLauncher.TEXTURE, this.getX(), this.getY(), UV_WIDTH, 111, this.width, this.height);
        }
        // Pressed state
        else if (this.wasPressed) {
            graphics.blit(GuiCruiseLauncher.TEXTURE, this.getX(), this.getY(), UV_WIDTH, 76, this.width, this.height);
        }
        // Hover state
        else if (hovered) {
            graphics.blit(GuiCruiseLauncher.TEXTURE, this.getX(), this.getY(), UV_WIDTH, 41, this.width, this.height);
        }
        // Default state
        else {
            graphics.blit(GuiCruiseLauncher.TEXTURE, this.getX(), this.getY(), UV_WIDTH, 6, this.width, this.height);
        }

        // Draw button cover
        if(!active && doDrawGlass) {
            graphics.blit(GuiCruiseLauncher.TEXTURE, this.getX() - 4, this.getY() - 4, 220, 5, 36, 36);
        }
    }

    public LaunchButton doDrawDisabledGlass() {
        this.doDrawGlass = true;
        return this;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.wasPressed = false;
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            this.wasPressed = true;
            return true;
        }
        return false;
    }
}
// PORTED
