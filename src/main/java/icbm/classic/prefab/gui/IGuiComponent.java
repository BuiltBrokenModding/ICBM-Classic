package icbm.classic.prefab.gui;

public interface IGuiComponent {

    /**
     * Callback for when this component is added to the container UI
     *
     * @param container added to
     */
    default void onAddedToHost(GuiContainerBase container) {

    }

    /**
     * Called each UI tick
     */
    default void onUpdate() {

    }

    /**
     * Called by {@link GuiContainerBase#drawScreen(int, int, float)}
     *
     * @param mouseX position
     * @param mouseY position
     * @param partialTicks delta between frame renders
     */
    default void draw(int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * Draws in the container's foreground layer
     *
     * @param mouseX position
     * @param mouseY position
     */
    default void drawForegroundLayer(int mouseX, int mouseY) {

    }

    /**
     * Draws in the container's foreground layer
     *
     * @param f ????
     * @param mouseX position
     * @param mouseY position
     */default void drawBackgroundLayer(float f, int mouseX, int mouseY) {

    }
}
