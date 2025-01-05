package icbm.classic.prefab.gui.textbox;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextFieldBase extends TextFieldWidget implements IGuiComponent {

    protected GuiContainerBase container;

    public GuiTextFieldBase(FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(fontrendererObj, x, y, par5Width, par6Height, "");
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }
}
