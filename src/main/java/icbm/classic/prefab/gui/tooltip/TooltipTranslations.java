package icbm.classic.prefab.gui.tooltip;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.chat.Component;

/**
 * Simple tooltip component for showing users additional information
 */
public class TooltipTranslations extends TooltipBase {
    private Component normalTooltip;
    private Component shiftTooltip;

    public TooltipTranslations(int x, int y, int width, int height, Component tooltip) {
        super(x, y, width, height);
        this.normalTooltip = tooltip;
    }

    public TooltipTranslations withShift(Component shiftTooltip) {
        this.shiftTooltip = shiftTooltip;
        return this;
    }

    @Override
    protected Component getActualTooltip() {
        if (GuiScreen.isShiftKeyDown()) {
            return shiftTooltip;
        }
        return normalTooltip;
    }
}
