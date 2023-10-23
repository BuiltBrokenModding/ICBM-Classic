package icbm.classic.prefab.gui.tooltip;

import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import lombok.Getter;
import net.minecraft.util.text.ITextComponent;

/**
 * Simple tooltip component for showing users additional information
 */
public abstract class TooltipBase implements IToolTip, IGuiComponent {

    /**
     * Bound box, relative to top-left of the container
     */
    @Getter
    private final Rectangle bounds;

    /**
     * Delay in seconds to wait to show tooltip
     */
    @Getter
    private float hoverDelay;

    private GuiContainerBase container;

    /**
     * Is mouse over component
     */
    @Getter
    private boolean isHovering = false;
    /**
     * Current hover tick in seconds
     */
    private float hoveringTicks = 0;

    public TooltipBase(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, x + width, y + height);
    }

    public TooltipBase withDelay(float delay) {
        this.hoverDelay = delay;
        return this;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        isHovering = isWithin(mouseX, mouseY);
        if (isHovering) {
            hoveringTicks += partialTicks;
        } else {
            hoveringTicks = 0;
        }

        //Gui.drawRect(bounds.getMin().xi() + container.getGuiLeft(), bounds.getMin().yi() + container.getGuiTop(), bounds.getMax().xi() + container.getGuiLeft(), bounds.getMax().yi() + container.getGuiTop(), -6250336);
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
    public final ITextComponent getTooltip() {
        if (hoveringTicks < hoverDelay) {
            return null;
        }
        return getActualTooltip();
    }

    protected abstract ITextComponent getActualTooltip();
}
