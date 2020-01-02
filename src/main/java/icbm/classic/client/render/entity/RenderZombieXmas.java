package icbm.classic.client.render.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.mobs.ModelZombieXmas;
import icbm.classic.content.entity.mobs.EntityXmasZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderZombieXmas extends RenderLiving<EntityXmasZombie>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, "textures/entity/zombie/zombie.hat.red.png");

    public RenderZombieXmas(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelZombieXmas(), 0.5F);
        //this.addLayer(new LayerHeldItemSkeletonXmas(this));
    }

    @Override
    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityXmasZombie entity)
    {
        return TEXTURE;
    }
}