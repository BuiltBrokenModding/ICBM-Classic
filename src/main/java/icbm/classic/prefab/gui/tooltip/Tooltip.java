package icbm.classic.prefab.gui.tooltip;

import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Simple tooltip component for showing users additional information
 */
@Data
@RequiredArgsConstructor
public class Tooltip implements IToolTip, IGuiComponent {

    /** Bound box, relative to top-left of the container */
    private final Rectangle bounds;
    /** Supplier for getting tooltip in real time */
    private final Supplier<ITextComponent> tooltip;
    /** Delay in seconds to wait to show tooltip */
    private final float hoverDelay;

    private GuiContainerBase container;

    /** Is mouse over component */
    private boolean isHovering = false;
    /** Current hover tick in seconds  */
    private float hoveringTicks = 0;

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        isHovering = isWithin(mouseX, mouseY);
        if(isHovering) {
            hoveringTicks += partialTicks;
        }
        else {
            hoveringTicks = 0;
        }
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    @Override
    public boolean isWithin(int x, int y) {
        return bounds.isWithin(x - container.getGuiLeft(), y - container.getGuiTop());
    }

    @Override
    public ITextComponent getTooltip() {
        if(hoveringTicks < hoverDelay) {
            return null;
        }
        return tooltip.get();
    }
}
