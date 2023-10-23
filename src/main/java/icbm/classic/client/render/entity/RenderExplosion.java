package icbm.classic.client.render.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.content.entity.EntityExplosion;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderExplosion extends Render<EntityExplosion>
{
    public static ResourceLocation GREY_TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "models/grey.png");

    public RenderExplosion(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityExplosion entityExplosion, double x, double y, double z, float entityYaw, float partialTicks)
    {
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityExplosion entity)
    {
        return GREY_TEXTURE;
    }
}
