package icbm.classic.prefab.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.prefab.gui.GuiContainerBase;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class RedstoneButton extends GuiButtonBase<RedstoneButton> {

    private final Supplier<Boolean> redstoneState;

    public RedstoneButton(int x, int y, Supplier<Boolean> redstoneState) {
        super(x, y, 12, 12, "");
        this.redstoneState = redstoneState;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            // Reset and bind texture
            Minecraft.getInstance().getTextureManager().bindTexture(GuiContainerBase.COMPONENTS_TEXTURE);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Get state
            final boolean redstoneEnabled = redstoneState.get();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            // Handle state
            int offset = 0;
            if(!this.active) {
                offset = 1;
            }
            else if(this.isHovered) {
                offset = 2;
            }

            // Render UV
            this.blit(this.x, this.y, (!redstoneEnabled ? 12 : 0), offset * 12, 12, 12);


            //this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
