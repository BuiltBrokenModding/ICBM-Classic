package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityFragments;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderShrapnel extends Render<EntityFragments>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/fragment.png");

    protected RenderShrapnel(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityFragments entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if (entity.isAnvil)
        {
            //TODO render anvil block
        }
        else
        {
            //TODO render shrapnel like arrows
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityFragments entity)
    {
        return TEXTURE_FILE;
    }
}
