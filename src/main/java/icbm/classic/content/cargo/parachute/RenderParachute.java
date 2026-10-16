package icbm.classic.content.cargo.parachute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icbm.classic.client.render.entity.item.RenderItemImp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class RenderParachute extends RenderItemImp<EntityParachute> {
    public RenderParachute(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void rotate(EntityParachute entity, ItemStack itemstack, PoseStack poseStack, float entityYaw, float entityPitch, float partialTicks, int index) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.getYRot()));
        // TODO add wiggle to simulate wind
    }

    @Override
    protected ItemStack getRenderItem(EntityParachute entity, int index) {
        return entity.getRenderItemStack();
    }

    @Override
    protected void scale(EntityParachute e, ItemStack itemstack, PoseStack poseStack, float partialTicks, int index) {
        if(e != null) {
            final float scale = e.getRenderScale();
            poseStack.scale(scale, scale, scale);
        }
    }
}
// PORTED
