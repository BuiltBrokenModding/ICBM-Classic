package icbm.classic.content.blocks.launcher.cruise.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.prefab.gui.button.GuiButtonBase;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class LaunchButton extends GuiButtonBase<LaunchButton> {

    private boolean wasPressed;
    private boolean doDrawGlass = false;
    private Supplier<Boolean> enabledCheck;

    public LaunchButton(int x, int y) {
        super(x, y, 28, 29, "");
    }

    public LaunchButton setEnabledCheck(Supplier<Boolean> supplier) {
        this.enabledCheck = supplier;
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (enabledCheck != null) {
            this.active = enabledCheck.get();
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        // Set color and texture
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(GuiCruiseLauncher.TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);


        final int UV_WIDTH = 182;

        // Disabled state
        if (!this.active) {
            this.blit(this.x, this.y, UV_WIDTH, 111, this.width, this.height);
        }
        // Pressed state
        else if (this.wasPressed) {
            this.blit(this.x, this.y, UV_WIDTH, 76, this.width, this.height);
        }
        // Hover state
        else if (this.isHovered) {
            this.blit(this.x, this.y, UV_WIDTH, 41, this.width, this.height);
        }
        // Default state
        else {
            this.blit(this.x, this.y, UV_WIDTH, 6, this.width, this.height);
        }

        // Draw button cover
        if (!active && doDrawGlass) {
            this.blit(this.x - 4, this.y - 4, 220, 5, 36, 36);
        }
    }

    public LaunchButton doDrawDisabledGlass() {
        this.doDrawGlass = true;
        return this;
    }

    @Override
    public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
        super.onRelease(p_onRelease_1_, p_onRelease_3_);
        this.wasPressed = false;
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        super.onClick(p_onClick_1_, p_onClick_3_);
        this.wasPressed = true;
    }
}
