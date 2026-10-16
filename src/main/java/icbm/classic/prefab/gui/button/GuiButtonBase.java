package icbm.classic.prefab.gui.button;

import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Base button with action callback and tooltip. Client only.
 * <p>
 * PORT: 1.12 GuiButton -> {@link Button}. Button ids are kept in the constructor but unused, actions run
 * from the button's own press handler instead of GuiScreen#actionPerformed. Coordinates are absolute.
 */
public class GuiButtonBase<B extends GuiButtonBase> extends Button implements IGuiComponent, IToolTip {

    private ActionTrigger action;

    private final Rectangle bounds;
    private Supplier<Component> tooltip;

    protected GuiContainerBase<?> container;

    public GuiButtonBase(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(x, y, widthIn, heightIn, Component.literal(buttonText), (button) -> ((GuiButtonBase<?>) button).triggerAction(), DEFAULT_NARRATION);
        bounds = new Rectangle(x, y, x + widthIn, y + heightIn);
    }

    @Override
    public void onAddedToHost(GuiContainerBase<?> container) {
        this.container = container;
    }

    public B setAction(ActionTrigger action) {
        this.action = action;
        return (B) this;
    }

    public B setTooltip(Supplier<Component> tooltip) {
        this.tooltip = tooltip;
        return (B) this;
    }

    public void triggerAction() {
        if(action != null) {
            action.trigger();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // PORT: 1.21 buttons press on Enter/Space once focused by a click, 1.12 GuiButton had no keyboard
        // activation. Blocks resending launch/fire packets and bypassing the Alt/Shift checks in mouseClicked.
        return false;
    }

    @Override
    public boolean isWithin(int x, int y) {
        return bounds.isWithin(x, y);
    }

    @Override
    public Component getToolTip() {
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
// PORTED
