package icbm.classic.prefab.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.ICBMConstants;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiContainerBase<T extends Container> extends ContainerScreen<T>
{
    public static final ResourceLocation COMPONENTS_TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_components.png");

    public GuiContainerBase(T container, PlayerInventory inv, ITextComponent titleIn)
    {
        super(container, inv, titleIn);
    }

    @Override
    public void tick() {
        super.tick();
        this.children.forEach(component -> {
            if(component instanceof ITickingWidget) {
                ((ITickingWidget) component).update();
            }
        });
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        this.getMinecraft().textureManager.bindTexture(this.getBackground());
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    public abstract ResourceLocation getBackground();
}
