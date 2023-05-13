package icbm.classic.prefab.gui.button;

import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class GuiButtonBase<B extends GuiButtonBase> extends GuiButton implements IGuiComponent, IToolTip {

    private ActionTrigger action;

    private final Rectangle bounds;
    private Supplier<ITextComponent> tooltip;

    private GuiContainerBase container;

    public GuiButtonBase(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        bounds = new Rectangle(x, y, x + widthIn, y + heightIn);
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    public B setAction(ActionTrigger action) {
        this.action = action;
        return (B) this;
    }

    public B setTooltip(Supplier<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return (B) this;
    }

    public void triggerAction() {
        if(action != null) {
            action.trigger();
        }
    }

    @Override
    public boolean isWithin(int x, int y) {
        return bounds.isWithin(x, y);
    }

    @Override
    public ITextComponent getTooltip() {
        if(tooltip != null) {
            return tooltip.get();
        }
        return null;
    }

    @FunctionalInterface
    public static interface ActionTrigger {

        void trigger();
    }
}
