package icbm.classic.prefab.gui.tooltip;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import lombok.Getter;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;

/**
 * Simple tooltip component for showing users additional information
 */
public abstract class TooltipBase implements IToolTip, IGuiComponent {

    int x;
    int y;
    int width;
    int height;
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
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

        // Dev debug to see tooltip area TODO make toggle driven via hotkey
        /*AbstractGui.fill(
            x + container.getGuiLeft(),
            y + container.getGuiTop(),
            x + width + container.getGuiLeft(),
            y + width + container.getGuiTop(),
            -6250336);*/
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    @Override
    public boolean isWithin(int x, int y) {
        return x - container.getGuiLeft() >= this.x
            && x - container.getGuiLeft() < this.x + this.width
            && y - container.getGuiTop() >= this.y
            && y - container.getGuiTop() < this.y + this.height;
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
