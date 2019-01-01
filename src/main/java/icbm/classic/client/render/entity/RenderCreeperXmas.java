package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.mobs.ModelCreeperXmas;
import icbm.classic.content.entity.mobs.EntityXmasCreeper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCreeperXmas extends RenderLiving<EntityXmasCreeper>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/entity/creeper/creeper.png");

    public RenderCreeperXmas(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelCreeperXmas(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityXmasCreeper entity)
    {
        return TEXTURE;
    }
}