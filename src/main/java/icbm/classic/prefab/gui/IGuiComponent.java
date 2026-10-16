package icbm.classic.prefab.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Component of a {@link GuiContainerBase}. Client only.
 * <p>
 * PORT: draw methods take the {@link GuiGraphics} of the frame. Key input is split into
 * {@link #onKeyTyped(int, int, int)} (key codes) as 1.21 routes typed characters to the focused widget.
 */
public interface IGuiComponent {

    /**
     * Callback for when this component is added to the container UI
     *
     * @param container added to
     */
    default void onAddedToHost(GuiContainerBase<?> container) {

    }

    /**
     * Called each UI tick
     */
    default void onUpdate() {

    }

    /**
     * Called by {@link GuiContainerBase#render(GuiGraphics, int, int, float)}
     *
     * @param graphics to draw with
     * @param mouseX position
     * @param mouseY position
     * @param partialTicks delta between frame renders
     */
    default void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * Draws in the container's foreground layer, pose is translated to the top-left of the container
     *
     * @param graphics to draw with
     * @param mouseX position
     * @param mouseY position
     */
    default void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {

    }

    /**
     * Draws in the container's background layer, absolute screen coordinates
     *
     * @param graphics to draw with
     * @param f partial ticks
     * @param mouseX position
     * @param mouseY position
     */
    default void drawBackgroundLayer(GuiGraphics graphics, float f, int mouseX, int mouseY) {

    }


    /**
     * Called when a key is pressed
     *
     * @param keyCode GLFW key code
     * @param scanCode scan code
     * @param modifiers key modifiers
     * @return true if consumed to prevent other interaction from firing
     */
    default boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Called when mouse is clicked, only for components that are not widgets
     *
     * @param mouseX position
     * @param mouseY position
     * @param mouseButton pressed
     */
    default void onMouseClick(int mouseX, int mouseY, int mouseButton) {

    }
}
// PORTED
