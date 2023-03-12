package icbm.classic.prefab.gui.tooltip;

import icbm.classic.lib.transform.region.Rectangle;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Simple tooltip component for showing users additional information
 */
public class Tooltip extends TooltipBase {

    /**
     * Supplier for getting tooltip in real time
     */
    private final Supplier<ITextComponent> tooltip;

    public Tooltip(int x, int y, int width, int height, Supplier<ITextComponent> tooltip) {
        super(x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    protected ITextComponent getActualTooltip() {
        return tooltip.get();
    }
}
