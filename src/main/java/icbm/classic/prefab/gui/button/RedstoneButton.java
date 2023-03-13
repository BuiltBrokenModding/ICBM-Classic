package icbm.classic.prefab.gui.button;

import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Supplier;

public class RedstoneButton extends GuiButtonBase<RedstoneButton> {

    private final Supplier<Boolean> redstoneState;

    public RedstoneButton(int buttonId, int x, int y, Supplier<Boolean> redstoneState) {
        super(buttonId, x, y, 12, 12, "");
        this.redstoneState = redstoneState;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            // Reset and bind texture
            mc.getTextureManager().bindTexture(GuiContainerBase.COMPONENTS_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Get state
            final boolean redstoneEnabled = redstoneState.get();
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            // Handle state
            int offset = 0;
            if(!this.enabled) {
                offset = 1;
            }
            else if(this.hovered) {
                offset = 2;
            }

            // Render UV
            this.drawTexturedModalRect(this.x, this.y, (!redstoneEnabled ? 12 : 0), offset * 12, 12, 12);


            this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
