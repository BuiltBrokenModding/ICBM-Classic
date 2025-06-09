package icbm.classic.prefab.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class DisableButton extends GuiButtonBase<DisableButton> {

    // Icon this designed to cover is 13x13
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;

    private static final int UV_Y = 241;
    private static final int UV_X = 0;

    /**
     * Checks if disable status should render, not the same as #enabled or #visble. Even though this causes it to stop rendering is true
     */
    private final Supplier<Boolean> shouldShowAsDisabled;

    public DisableButton(int x, int y, String label, Supplier<Boolean> shouldShowAsDisabled) {
        super(x, y, WIDTH, HEIGHT, label);
        this.shouldShowAsDisabled = shouldShowAsDisabled;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {

        // Set color and texture
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(GuiContainerBase.COMPONENTS_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //TODO add disabled state

        // Hover state
        if (this.isHovered) {
            this.blit(this.x, this.y, UV_X + WIDTH, UV_Y, this.width, this.height);
        }
        // Default state
        else if (shouldShowAsDisabled.get()) {
            this.blit(this.x, this.y, UV_X, UV_Y, this.width, this.height);
        }
    }
}
