package icbm.classic.client.render.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.mobs.ModelSnowmanXmas;
import icbm.classic.content.entity.mobs.EntityXmasSnowman;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 12/31/2018.
 */
public class RenderSnowmanXmas extends RenderLiving<EntityXmasSnowman>
{
    private static final ResourceLocation SNOW_MAN_TEXTURES = new ResourceLocation(ICBMConstants.DOMAIN, "textures/entity/snowman/snowman.hat.green.png");

    public RenderSnowmanXmas(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelSnowmanXmas(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityXmasSnowman entity)
    {
        return SNOW_MAN_TEXTURES;
    }

    @Override
    public ModelSnowmanXmas getMainModel()
    {
        return (ModelSnowmanXmas) super.getMainModel();
    }
}
