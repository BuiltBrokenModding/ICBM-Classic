package icbm.classic.prefab.gui.tooltip;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Simple tooltip component for showing users additional information
 */
public class Tooltip extends TooltipBase {

    /**
     * Supplier for getting tooltip in real time
     */
    private final Supplier<Component> tooltip;

    public Tooltip(int x, int y, int width, int height, Supplier<Component> tooltip) {
        super(x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    protected Component getActualTooltip() {
        return tooltip.get();
    }
}
