package icbm.classic.prefab.gui;

import icbm.classic.lib.colors.ColorHelper;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.function.Consumer;

public class TextInput extends GuiTextField {

    private final static int ERROR_COLOR = ColorHelper.toARGB(255, 1, 1, 255);

    @Setter
    private Consumer<Boolean> focusChangedCallback;

    @Setter
    @Accessors(fluent = true)
    private boolean isErrored = false;

    public TextInput(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void setFocused(boolean isFocusedIn) {
        super.setFocused(isFocusedIn);
        if (focusChangedCallback != null) {
            focusChangedCallback.accept(isFocusedIn);
        }
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        if (this.getVisible() && this.getEnableBackgroundDrawing() && isErrored) {
            drawHorizontalLine(this.x, this.x + this.width, this.y + this.height, ERROR_COLOR);
        }
    }
}
