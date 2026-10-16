package icbm.classic.client.render.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.content.entity.EntityExplosion;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Renders nothing, the explosion controller entity is invisible */
public class RenderExplosion extends EntityRenderer<EntityExplosion>
{
    public static ResourceLocation GREY_TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "models/grey.png");

    public RenderExplosion(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public boolean shouldRender(EntityExplosion entity, net.minecraft.client.renderer.culling.Frustum frustum, double camX, double camY, double camZ)
    {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityExplosion entity)
    {
        return GREY_TEXTURE;
    }
}
// PORTED
