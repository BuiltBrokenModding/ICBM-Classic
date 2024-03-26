package icbm.classic.client.render.entity;

import icbm.classic.world.entity.SmokeEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSmoke extends Render<SmokeEntity> {
    public RenderSmoke(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(SmokeEntity seat, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(seat, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(SmokeEntity entity) {
        return null;
    }
}