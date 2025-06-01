package icbm.classic.prefab.gui;

/**
 * @deprecated Replaced with {@link net.minecraft.client.gui.widget.Widget}
 */
@Deprecated
public interface IGuiComponent extends ITickingWidget {

    /**
     * Called by {@link GuiContainerBase#drawScreen(int, int, float)}
     *
     * @param mouseX position
     * @param mouseY position
     * @param partialTicks delta between frame renders
     */
    default void draw(int mouseX, int mouseY, float partialTicks) {

    }
}
