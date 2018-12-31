package icbm.classic.client.models;

import icbm.classic.content.entity.EntityXmasSkeletonBoss;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

/**
 * ModelSkeleton - Mojang for skeleton and M1W3st for hat
 * Created using Tabula 7.0.0
 */
public class ModelSkeletonXmas extends ModelBiped
{
    public ModelRenderer shape8;
    public ModelRenderer shape9;
    public ModelRenderer shape10;
    public ModelRenderer shape11;
    public ModelRenderer shape12;
    public ModelRenderer shape13;
    public ModelRenderer shape14;
    public ModelRenderer shape15;

    public ModelSkeletonXmas(float modelSize, boolean p_i46303_2_)
    {
        super(modelSize, 0.0F, 64, 64);

        if (!p_i46303_2_)
        {
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedLeftArm = new ModelRenderer(this, 40, 16);
            this.bipedLeftArm.mirror = true;
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightLeg = new ModelRenderer(this, 0, 16);
            this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
            this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
            this.bipedLeftLeg.mirror = true;
            this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        }

        initHat();
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        this.rightArmPose = ModelBiped.ArmPose.EMPTY;
        this.leftArmPose = ModelBiped.ArmPose.EMPTY;
        ItemStack itemstack = entitylivingbaseIn.getHeldItem(EnumHand.MAIN_HAND);

        if (itemstack.getItem() == Items.BOW && ((AbstractSkeleton) entitylivingbaseIn).isSwingingArms())
        {
            if (entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            else
            {
                this.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }

        super.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
         /*
        ItemStack itemstack = ((EntityLivingBase) entityIn).getHeldItemMainhand();
        AbstractSkeleton abstractskeleton = (AbstractSkeleton) entityIn;


        if (abstractskeleton.isSwingingArms() && (itemstack.isEmpty() || itemstack.getItem() != Items.BOW))
        {
            float f = MathHelper.sin(this.swingProgress * (float) Math.PI);
            float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float) Math.PI);
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
            this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
            this.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F);
            this.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F);
            this.bipedRightArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
            this.bipedLeftArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
            this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
            this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        }
        */
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        GlStateManager.pushMatrix();
        if (entity instanceof EntityXmasSkeletonBoss)
        {
            GlStateManager.scale(1.9F, 1.9F, 1.9F);
            GlStateManager.translate(0.0F, -12.0F * scale, 0.0F);
            this.bipedHead.render(scale);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            this.bipedHeadwear.render(scale);
        }
        else
        {
            GlStateManager.scale(0.7F, 0.7F, 0.7F);
            GlStateManager.translate(0.0F, 10.0F * scale, 0.0F);
            this.bipedHead.render(scale);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            this.bipedHeadwear.render(scale);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void postRenderArm(float scale, EnumHandSide side)
    {
        float f = side == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        ModelRenderer modelrenderer = this.getArmForSide(side);
        modelrenderer.rotationPointX += f;
        modelrenderer.postRender(scale);
        modelrenderer.rotationPointX -= f;
    }

    protected void initHat()
    {
        this.shape11 = new ModelRenderer(this, 4, 42);
        this.shape11.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape11.addBox(0.0F, 0.0F, -2.5F, 4, 3, 5, 0.0F);

        this.setRotateAngle(shape11, 0.0F, 0.0F, 0.7740535232594852F);
        this.shape9 = new ModelRenderer(this, 0, 40);
        this.shape9.setRotationPoint(0.0F, -1.5F, 0.0F);
        this.shape9.addBox(-4.0F, 0.0F, -4.0F, 8, 2, 8, 0.0F);

        this.shape14 = new ModelRenderer(this, 0, 44);
        this.shape14.setRotationPoint(2.0F, 0.5F, -0.5F);
        this.shape14.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);

        this.shape12 = new ModelRenderer(this, 0, 42);
        this.shape12.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape12.addBox(0.0F, 0.0F, -2.0F, 3, 2, 4, 0.0F);
        this.setRotateAngle(shape12, 0.0F, 0.0F, 0.22759093446006054F);

        this.shape13 = new ModelRenderer(this, 0, 43);
        this.shape13.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.shape13.addBox(0.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape13, 0.0F, 0.0F, 0.8196066167365371F);

        this.shape10 = new ModelRenderer(this, 3, 40);
        this.shape10.setRotationPoint(-4.0F, 0.0F, 0.0F);
        this.shape10.addBox(0.0F, 0.0F, -3.0F, 4, 4, 6, 0.0F);
        this.setRotateAngle(shape10, 0.0F, 0.0F, -0.6373942428283291F);

        this.shape8 = new ModelRenderer(this, 0, 51);
        this.shape8.setRotationPoint(0.0F, -9.0F, 0.0F);
        this.shape8.addBox(-5.0F, 0.0F, -5.0F, 10, 3, 10, 0.0F);

        this.shape15 = new ModelRenderer(this, 13, 58);
        this.shape15.setRotationPoint(0.7F, 0.6F, 0.0F);
        this.shape15.addBox(0.0F, -1.0F, -0.5F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape15, 0.0F, 0.0F, 0.5009094953223726F);


        this.shape8.addChild(this.shape9);
        this.shape9.addChild(this.shape10);
        this.shape10.addChild(this.shape11);
        this.shape11.addChild(this.shape12);
        this.shape12.addChild(this.shape13);
        this.shape13.addChild(this.shape14);
        this.shape14.addChild(this.shape15);

        this.bipedHead.addChild(this.shape8);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
