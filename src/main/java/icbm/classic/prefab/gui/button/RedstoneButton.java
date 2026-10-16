package icbm.classic.prefab.gui.button;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

public class RedstoneButton extends GuiButtonBase<RedstoneButton> {

    private final Supplier<Boolean> redstoneState;

    public RedstoneButton(int buttonId, int x, int y, Supplier<Boolean> redstoneState) {
        super(buttonId, x, y, 12, 12, "");
        this.redstoneState = redstoneState;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        // Get state
        final boolean redstoneEnabled = redstoneState.get();
        final boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        // Handle state
        int offset = 0;
        if(!this.active) {
            offset = 1;
        }
        else if(hovered) {
            offset = 2;
        }

        // Render UV
        graphics.blit(GuiContainerBase.COMPONENTS_TEXTURE, this.getX(), this.getY(), (!redstoneEnabled ? 12 : 0), offset * 12, 12, 12);
    }
}
// PORTED
