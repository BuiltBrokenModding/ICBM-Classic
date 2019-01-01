package icbm.classic.client.models.mobs;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * ModelCreeper - Either Mojang or a mod author
 * Created using Tabula 7.0.0
 */
public class ModelCreeperXmas extends ModelBase
{
    public ModelRenderer head;
    public ModelRenderer body;

    public ModelRenderer leg1;
    public ModelRenderer leg2;
    public ModelRenderer leg3;
    public ModelRenderer leg4;

    public ModelRenderer NeckPlatform;
    public ModelRenderer NeckPlatform_1;
    public ModelRenderer shape28;
    public ModelRenderer Neck;
    public ModelRenderer MinigunBody;
    public ModelRenderer Barrel1;
    public ModelRenderer Barrel2;
    public ModelRenderer Barrel3;
    public ModelRenderer Barrel4;
    public ModelRenderer MuzzleBreak;
    public ModelRenderer Neck_1;
    public ModelRenderer MinigunBody_1;
    public ModelRenderer Barrel1_1;
    public ModelRenderer Barrel2_1;
    public ModelRenderer Barrel3_1;
    public ModelRenderer Barrel4_1;
    public ModelRenderer MuzzleBreak_1;
    public ModelRenderer shape28_1;
    public ModelRenderer shape28_2;
    public ModelRenderer shape28_3;
    public ModelRenderer shape8;
    public ModelRenderer shape9;
    public ModelRenderer shape10;
    public ModelRenderer shape11;
    public ModelRenderer shape12;
    public ModelRenderer shape13;
    public ModelRenderer shape14;
    public ModelRenderer shape15;

    public ModelCreeperXmas()
    {
        this.textureWidth = 64;
        this.textureHeight = 100;
        this.MinigunBody = new ModelRenderer(this, 0, 32);
        this.MinigunBody.setRotationPoint(2.0F, -1.0F, 1.5F);
        this.MinigunBody.addBox(-2.0F, -4.0F, -3.0F, 4, 4, 6, 0.0F);
        this.NeckPlatform = new ModelRenderer(this, 0, 32);
        this.NeckPlatform.setRotationPoint(-5.0F, 4.5F, -2.0F);
        this.NeckPlatform.addBox(0.0F, 0.0F, 0.0F, 4, 1, 4, 0.0F);
        this.setRotateAngle(NeckPlatform, 0.0F, 0.017453292519943295F, -1.5707963267948966F);
        this.shape15 = new ModelRenderer(this, 13, 58);
        this.shape15.setRotationPoint(0.7F, 0.6F, 0.0F);
        this.shape15.addBox(0.0F, -1.0F, -0.5F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape15, 0.0F, 0.0F, 0.5009094953223726F);
        this.MuzzleBreak = new ModelRenderer(this, 0, 32);
        this.MuzzleBreak.setRotationPoint(0.0F, -2.0F, -7.5F);
        this.MuzzleBreak.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 1, 0.0F);
        this.shape28_3 = new ModelRenderer(this, 0, 32);
        this.shape28_3.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shape28_3.addBox(-5.0F, 0.0F, 0.0F, 10, 1, 1, 0.0F);
        this.Neck_1 = new ModelRenderer(this, 0, 32);
        this.Neck_1.setRotationPoint(2.0F, 0.0F, 2.0F);
        this.Neck_1.addBox(-1.5F, -2.0F, -1.5F, 3, 2, 3, 0.0F);
        this.body = new ModelRenderer(this, 16, 16);
        this.body.setRotationPoint(0.0F, 6.0F, 0.0F);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.Barrel2_1 = new ModelRenderer(this, 0, 32);
        this.Barrel2_1.setRotationPoint(-1.0F, -1.0F, -2.0F);
        this.Barrel2_1.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.shape28_1 = new ModelRenderer(this, 0, 32);
        this.shape28_1.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shape28_1.addBox(-5.0F, 0.0F, 0.0F, 10, 1, 1, 0.0F);
        this.Barrel3 = new ModelRenderer(this, 0, 32);
        this.Barrel3.setRotationPoint(0.5F, -3.0F, -2.0F);
        this.Barrel3.addBox(0.0F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.leg4 = new ModelRenderer(this, 0, 16);
        this.leg4.setRotationPoint(-2.0F, 18.0F, 4.0F);
        this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.leg3 = new ModelRenderer(this, 0, 16);
        this.leg3.setRotationPoint(-2.0F, 18.0F, -4.0F);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.Barrel1 = new ModelRenderer(this, 0, 32);
        this.Barrel1.setRotationPoint(-1.0F, -3.0F, -2.0F);
        this.Barrel1.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.MuzzleBreak_1 = new ModelRenderer(this, 0, 32);
        this.MuzzleBreak_1.setRotationPoint(0.0F, -2.0F, -7.5F);
        this.MuzzleBreak_1.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 1, 0.0F);
        this.shape28 = new ModelRenderer(this, 0, 32);
        this.shape28.setRotationPoint(0.0F, 1.25F, -3.0F);
        this.shape28.addBox(-5.0F, 0.0F, 0.0F, 10, 1, 1, 0.0F);
        this.Barrel2 = new ModelRenderer(this, 0, 32);
        this.Barrel2.setRotationPoint(-1.0F, -1.0F, -2.0F);
        this.Barrel2.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.shape9 = new ModelRenderer(this, 0, 44);
        this.shape9.setRotationPoint(0.0F, -1.5F, 0.0F);
        this.shape9.addBox(-4.0F, 0.0F, -4.0F, 8, 2, 8, 0.0F);
        this.head = new ModelRenderer(this, 0, 0);
        this.head.setRotationPoint(0.0F, 6.0F, 0.0F);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.shape13 = new ModelRenderer(this, 6, 45);
        this.shape13.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.shape13.addBox(0.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape13, 0.0F, 0.0F, 0.8196066167365371F);
        this.leg2 = new ModelRenderer(this, 0, 16);
        this.leg2.setRotationPoint(2.0F, 18.0F, -4.0F);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.Barrel3_1 = new ModelRenderer(this, 0, 32);
        this.Barrel3_1.setRotationPoint(0.5F, -3.0F, -2.0F);
        this.Barrel3_1.addBox(0.0F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.Barrel1_1 = new ModelRenderer(this, 0, 32);
        this.Barrel1_1.setRotationPoint(-1.0F, -3.0F, -2.0F);
        this.Barrel1_1.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.shape28_2 = new ModelRenderer(this, 0, 32);
        this.shape28_2.setRotationPoint(0.0F, 0.0F, 5.0F);
        this.shape28_2.addBox(-5.0F, 0.0F, 0.0F, 10, 1, 1, 0.0F);
        this.Barrel4_1 = new ModelRenderer(this, 0, 32);
        this.Barrel4_1.setRotationPoint(1.0F, -1.0F, -2.0F);
        this.Barrel4_1.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.MinigunBody_1 = new ModelRenderer(this, 0, 32);
        this.MinigunBody_1.setRotationPoint(2.0F, -1.0F, 1.5F);
        this.MinigunBody_1.addBox(-2.0F, -4.0F, -3.0F, 4, 4, 6, 0.0F);
        this.shape12 = new ModelRenderer(this, 0, 47);
        this.shape12.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape12.addBox(0.0F, 0.0F, -2.0F, 3, 2, 4, 0.0F);
        this.setRotateAngle(shape12, 0.0F, 0.0F, 0.22759093446006054F);
        this.leg1 = new ModelRenderer(this, 0, 16);
        this.leg1.setRotationPoint(2.0F, 18.0F, 4.0F);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.shape8 = new ModelRenderer(this, 0, 55);
        this.shape8.setRotationPoint(0.0F, -9.0F, 0.0F);
        this.shape8.addBox(-5.0F, 0.0F, -5.0F, 10, 3, 10, 0.0F);
        this.Neck = new ModelRenderer(this, 0, 32);
        this.Neck.setRotationPoint(2.0F, 0.0F, 2.0F);
        this.Neck.addBox(-1.5F, -2.0F, -1.5F, 3, 2, 3, 0.0F);
        this.shape11 = new ModelRenderer(this, 4, 45);
        this.shape11.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape11.addBox(0.0F, 0.0F, -2.5F, 4, 3, 5, 0.0F);
        this.setRotateAngle(shape11, 0.0F, 0.0F, 0.7740535232594852F);
        this.NeckPlatform_1 = new ModelRenderer(this, 0, 32);
        this.NeckPlatform_1.setRotationPoint(5.0F, 0.5F, -2.0F);
        this.NeckPlatform_1.addBox(0.0F, 0.0F, 0.0F, 4, 1, 4, 0.0F);
        this.setRotateAngle(NeckPlatform_1, 0.0F, 0.0F, 1.5707963267948966F);
        this.shape14 = new ModelRenderer(this, 5, 49);
        this.shape14.setRotationPoint(2.0F, 0.5F, -0.5F);
        this.shape14.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.Barrel4 = new ModelRenderer(this, 0, 32);
        this.Barrel4.setRotationPoint(1.0F, -1.0F, -2.0F);
        this.Barrel4.addBox(-0.5F, -0.5F, -6.0F, 1, 1, 6, 0.0F);
        this.shape10 = new ModelRenderer(this, 3, 44);
        this.shape10.setRotationPoint(-4.0F, 0.0F, 0.0F);
        this.shape10.addBox(0.0F, 0.0F, -3.0F, 4, 4, 6, 0.0F);
        this.setRotateAngle(shape10, 0.0F, 0.0F, -0.6373942428283291F);
        this.NeckPlatform.addChild(this.MinigunBody);
        this.body.addChild(this.NeckPlatform);
        this.shape14.addChild(this.shape15);
        this.MinigunBody.addChild(this.MuzzleBreak);
        this.shape28_2.addChild(this.shape28_3);
        this.NeckPlatform_1.addChild(this.Neck_1);
        this.MinigunBody_1.addChild(this.Barrel2_1);
        this.shape28.addChild(this.shape28_1);
        this.MinigunBody.addChild(this.Barrel3);
        this.MinigunBody.addChild(this.Barrel1);
        this.MinigunBody_1.addChild(this.MuzzleBreak_1);
        this.body.addChild(this.shape28);
        this.MinigunBody.addChild(this.Barrel2);
        this.shape8.addChild(this.shape9);
        this.shape12.addChild(this.shape13);
        this.MinigunBody_1.addChild(this.Barrel3_1);
        this.MinigunBody_1.addChild(this.Barrel1_1);
        this.shape28.addChild(this.shape28_2);
        this.MinigunBody_1.addChild(this.Barrel4_1);
        this.NeckPlatform_1.addChild(this.MinigunBody_1);
        this.shape11.addChild(this.shape12);
        this.head.addChild(this.shape8);
        this.NeckPlatform.addChild(this.Neck);
        this.shape10.addChild(this.shape11);
        this.body.addChild(this.NeckPlatform_1);
        this.shape13.addChild(this.shape14);
        this.MinigunBody.addChild(this.Barrel4);
        this.shape9.addChild(this.shape10);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleX = headPitch * 0.017453292F;

        this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    }

    public void render(float scale)
    {
        this.leg4.render(scale);
        this.leg1.render(scale);
        this.body.render(scale);
        this.leg2.render(scale);
        this.leg3.render(scale);
        this.head.render(scale);
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
