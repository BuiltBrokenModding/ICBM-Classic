package icbm.classic.prefab.gui.textbox;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiTextFieldBase extends GuiTextField implements IGuiComponent {

    protected GuiContainerBase container;

    public GuiTextFieldBase(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        final int cursorX = mouseX - container.getGuiLeft();
        final int cursorY = mouseY - container.getGuiTop();
        super.mouseClicked(cursorX, cursorY, mouseButton);
    }

    @Override
    public void drawForegroundLayer(int mouseX, int mouseY) {
        drawTextBox();
    }

    @Override
    public boolean onKeyTyped(char key, int keyId) {
        if (isFocused()) {
            textboxKeyTyped(key, keyId);
            return true;
        }
        return false;
    }
}
