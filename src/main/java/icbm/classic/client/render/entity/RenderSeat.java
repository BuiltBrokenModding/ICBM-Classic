package icbm.classic.client.render.entity;

import icbm.classic.world.entity.PlayerSeatEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSeat extends Render<PlayerSeatEntity> {
    public RenderSeat(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(PlayerSeatEntity seat, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(seat, x, y, z, entityYaw, partialTicks);
        //GlStateManager.pushMatrix();
        //renderOffsetAABB(seat.getEntityBoundingBox(), x - seat.lastTickPosX, y - seat.lastTickPosY, z - seat.lastTickPosZ);
        //GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(PlayerSeatEntity entity) {
        return null;
    }
}