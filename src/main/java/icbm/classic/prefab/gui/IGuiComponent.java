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
     * Draws in the container's foreground layer
     *
     * @param mouseX relative to left of the container
     * @param mouseY relative to the top of the container
     */
    default void drawForegroundLayer(int mouseX, int mouseY) {

    }

    /**
     * Draws in the container's foreground layer
     *
     * @param f ????
     * @param mouseX relative to left of the container
     * @param mouseY relative to the top of the container
     */
    default void drawBackgroundLayer(float f, int mouseX, int mouseY) {

    }

    /**
     * Called when a key is pressed
     *
     * @param key character
     * @param keyId id
     * @return true if consumed to prevent other interaction from firing
     */
    default boolean onKeyTyped(char key, int keyId) {
        return false;
    }

    /**
     * Called when mouse is clicked
     *
     * @param mouseX relative to left of the container
     * @param mouseY relative to the top of the container
     * @param mouseButton pressed
     */
    default void onMouseClick(int mouseX, int mouseY, int mouseButton) {

    }
}
