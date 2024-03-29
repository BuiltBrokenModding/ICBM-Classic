package icbm.classic.prefab.gui.tooltip;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;

/**
 * Simple tooltip component for showing users additional information
 */
public class TooltipTranslations extends TooltipBase {
    private ITextComponent normalTooltip;
    private ITextComponent shiftTooltip;

    public TooltipTranslations(int x, int y, int width, int height, ITextComponent tooltip) {
        super(x, y, width, height);
        this.normalTooltip = tooltip;
    }

    public TooltipTranslations withShift(ITextComponent shiftTooltip) {
        this.shiftTooltip = shiftTooltip;
        return this;
    }

    @Override
    protected ITextComponent getActualTooltip() {
        if(GuiScreen.isShiftKeyDown()) {
            return shiftTooltip;
        }
        return normalTooltip;
    }
}
