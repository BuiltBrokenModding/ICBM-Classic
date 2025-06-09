package icbm.classic.prefab.gui.button;

import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class GuiButtonBase<B extends GuiButtonBase> extends AbstractButton {

    private ActionTrigger action;

    private Supplier<ITextComponent> tooltip;

    public GuiButtonBase(int x, int y, int widthIn, int heightIn, String buttonText) {
        super(x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void onPress() {
        if(action != null) {
            action.trigger();
        }
    }

    public B setAction(ActionTrigger action) {
        this.action = action;
        return (B) this;
    }

    public B setTooltip(Supplier<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return (B) this;
    }

    @FunctionalInterface
    public static interface ActionTrigger {

        void trigger();
    }
}
