package icbm.classic.prefab.gui.tooltip;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Simple tooltip component for showing users additional information
 */
public class Tooltip<GUI extends GuiContainerBase> extends TooltipBase<GUI> {

    /**
     * Supplier for getting tooltip in real time
     */
    private final Supplier<ITextComponent> tooltip;

    public Tooltip(GUI host, int x, int y, int width, int height, Supplier<ITextComponent> tooltip) {
        super(host, x, y, width, height);
        this.tooltip = tooltip;
    }

    @Override
    protected ITextComponent getTooltip() {
        return tooltip.get();
    }
}
