package icbm.classic.prefab.gui.textbox;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Text field component. Client only.
 * <p>
 * PORT: 1.12 GuiTextField was drawn in the foreground layer using gui relative coordinates. The 1.21
 * {@link EditBox} is a widget with absolute coordinates, so the relative position is kept and converted
 * when added to the container. Typed characters reach the box through the screen's focus handling.
 */
public class GuiTextFieldBase extends EditBox implements IGuiComponent {

    protected GuiContainerBase<?> container;

    /** Position relative to the top-left of the container */
    protected final int relativeX;
    protected final int relativeY;

    /** Mirrors EditBox's border flag, whose getter is not public in 1.21.1 */
    private boolean bordered = true;

    public GuiTextFieldBase(int componentId, Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
        this.relativeX = x;
        this.relativeY = y;
    }

    @Override
    public void onAddedToHost(GuiContainerBase<?> container) {
        this.container = container;
        this.setX(container.getGuiLeft() + relativeX);
        this.setY(container.getGuiTop() + relativeY);
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        // Consume keys while typing so the inventory key doesn't close the screen
        if(isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return false;
    }

    @Override
    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        super.setBordered(bordered);
    }

    // PORT-CHECK: replaces EditBox#isBordered, which may be private in 1.21.1
    public boolean hasBorder() {
        return bordered;
    }

    /** 1.12 name, kept for callers */
    public String getText() {
        return getValue();
    }

    /** 1.12 name, kept for callers */
    public void setText(String text) {
        setValue(text);
    }
}
// PORTED
