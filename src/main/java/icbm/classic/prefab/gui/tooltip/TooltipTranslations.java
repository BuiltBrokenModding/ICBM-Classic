package icbm.classic.prefab.gui.tooltip;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

/**
 * Simple tooltip component for showing users additional information
 */
public class TooltipTranslations<GUI extends GuiContainerBase> extends TooltipBase<GUI> {
    private ITextComponent normalTooltip;
    private ITextComponent shiftTooltip;

    public TooltipTranslations(GUI host, int x, int y, int width, int height, ITextComponent tooltip) {
        super(host, x, y, width, height);
        this.normalTooltip = tooltip;
    }

    public TooltipTranslations withShift(ITextComponent shiftTooltip) {
        this.shiftTooltip = shiftTooltip;
        return this;
    }

    @Override
    protected ITextComponent getTooltip() {
        if(Screen.hasShiftDown()) {
            return shiftTooltip;
        }
        return normalTooltip;
    }
}
