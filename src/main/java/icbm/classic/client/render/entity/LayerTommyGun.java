package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelTommyGun;
import icbm.classic.content.entity.EntityXmasSkeleton;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerTommyGun implements LayerRenderer<EntityXmasSkeleton>
{
    protected final RenderLivingBase<?> livingEntityRenderer;

    public static final ModelTommyGun gun = new ModelTommyGun();
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/gun.tommy.png");

    public LayerTommyGun(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityXmasSkeleton entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        GlStateManager.pushMatrix();

        //Scale for mob

        //Move to hand
        //this.translateToHand(EnumHandSide.RIGHT);

        //Rotate
        //GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        //GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        //Fix offset
        GlStateManager.scale(-1, 1, 1);
        //GlStateManager.translate(1 / 16.0F, 0.125F, -0.625F);
        //GlStateManager.translate(-1, 5, -1);

        //Render gun
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(ICBMClassic.DOMAIN, "textures/grey.png"));
        //GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        gun.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F);

        GlStateManager.popMatrix();
    }

    protected void translateToHand(EnumHandSide p_191361_1_)
    {
        ((ModelBiped) this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F, p_191361_1_);
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}