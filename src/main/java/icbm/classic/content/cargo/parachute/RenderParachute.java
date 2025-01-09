package icbm.classic.content.cargo.parachute;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.client.render.entity.item.RenderItemImp;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.item.ItemStack;

public class RenderParachute extends RenderItemImp<EntityParachute> {
    private final float renderScale;
    public RenderParachute(EntityRendererManager renderManagerIn, float renderScale) {
        super(renderManagerIn);
        this.renderScale = renderScale;
    }

    @Override
    protected void rotate(EntityParachute entity, float entityYaw, float entityPitch, float partialTicks) {
        GlStateManager.rotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        // TODO add wiggle to simulate wind
    }

    @Override
    protected ItemStack getRenderItem(EntityParachute entity) {
        return entity.getRenderItemStack().orElse(ItemStack.EMPTY);
    }

    @Override
    protected void scale(EntityParachute e, float partialTicks) {
        if(e != null) {
            GlStateManager.scalef(renderScale, renderScale, renderScale);
        }
    }
}
