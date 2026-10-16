package icbm.classic.prefab.gui.button;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

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
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Check if mouse is over button TODO check for circle
        final boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        //TODO add disabled state

        // Hover state
        if (hovered && Screen.hasAltDown()) {
            graphics.blit(GuiContainerBase.COMPONENTS_TEXTURE, this.getX(), this.getY(), UV_X + WIDTH, UV_Y, this.width, this.height);
        }
        // Default state
        else if(shouldShowAsDisabled.get()) {
            graphics.blit(GuiContainerBase.COMPONENTS_TEXTURE, this.getX(), this.getY(), UV_X, UV_Y, this.width, this.height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return Screen.hasAltDown() && super.mouseClicked(mouseX, mouseY, button);
    }
}
// PORTED
