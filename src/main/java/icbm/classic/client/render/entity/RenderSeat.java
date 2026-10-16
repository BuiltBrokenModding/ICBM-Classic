package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import icbm.classic.content.entity.EntityPlayerSeat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class RenderSeat extends EntityRenderer<EntityPlayerSeat>
{
    public RenderSeat(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityPlayerSeat seat, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(seat, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /** 1.12 returned null, 1.21 requires a non-null texture which is never bound since nothing is drawn */
    @Override
    public ResourceLocation getTextureLocation(EntityPlayerSeat entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
// PORTED
