package icbm.classic.prefab.gui.tooltip;

import net.minecraft.network.chat.Component;

/**
 * PORT: 1.12 {@code getTooltip()} is renamed {@link #getToolTip()} because 1.21 AbstractWidget already
 * declares {@code Tooltip getTooltip()} and ICBM buttons/text boxes are widgets.
 */
public interface IToolTip {

    /**
     * Checks if the tooltip contains the cursor
     *
     * @param x of the mouse relative to the screen
     * @param y of the mouse relative to the screen
     * @return true if within component bounds
     */
    boolean isWithin(int x, int y);

    /**
     * Tooltip to display
     *
     * @return tooltip or null to ignore
     */
    Component getToolTip();
}
// PORTED
