package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelSkeletonXmas;
import icbm.classic.content.entity.EntityXmasSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSkeletonXmas extends RenderLiving<EntityXmasSkeleton>
{
    private static final ResourceLocation SKELETON_HAT_RED_TEXTURES = new ResourceLocation(ICBMClassic.DOMAIN, "textures/entity/skeleton/skeleton.hat.red.png");
    private static final ResourceLocation SKELETON_HAT_GREEN_TEXTURES = new ResourceLocation(ICBMClassic.DOMAIN, "textures/entity/skeleton/skeleton.hat.green.png");

    public RenderSkeletonXmas(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelSkeletonXmas(0, false), 0.5F);
        //this.addLayer(new LayerTommyGun(this));
        this.addLayer(new LayerHeldItemSkeletonXmas(this));
        /*
        this.addLayer(new LayerBipedArmor(this)
        {
            protected void initArmor()
            {
                this.modelLeggings = new ModelSkeletonXmas(0.5F, true);
                this.modelArmor = new ModelSkeletonXmas(1.0F, true);
            }
        });
        */
    }

    @Override
    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityXmasSkeleton entity)
    {
        return SKELETON_HAT_GREEN_TEXTURES;
    }
}