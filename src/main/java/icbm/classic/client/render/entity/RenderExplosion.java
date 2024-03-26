package icbm.classic.client.render.entity;

import icbm.classic.IcbmConstants;
import icbm.classic.world.entity.ExplosionEntity;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderExplosion extends Render<ExplosionEntity> {
    public static ResourceLocation GREY_TEXTURE = new ResourceLocation(IcbmConstants.MOD_ID, IcbmConstants.TEXTURE_DIRECTORY + "models/grey.png");

    public RenderExplosion(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(ExplosionEntity entityExplosion, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ExplosionEntity entity) {
        return GREY_TEXTURE;
    }
}
