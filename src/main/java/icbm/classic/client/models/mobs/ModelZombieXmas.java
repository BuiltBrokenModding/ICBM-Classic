package icbm.classic.client.models.mobs;

import icbm.classic.content.entity.mobs.EntityXmasZombieBoss;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * ModelZombie - Either Mojang or a mod author
 * Created using Tabula 7.0.0
 */
public class ModelZombieXmas extends ModelBase
{
    public ModelRenderer bipedRightArm;
    public ModelRenderer bipedRightLeg;
    public ModelRenderer bipedHead;
    public ModelRenderer bipedBody;
    public ModelRenderer bipedLeftArm;
    public ModelRenderer bipedLeftLeg;

    public ModelRenderer shape8;
    public ModelRenderer shape9;
    public ModelRenderer shape10;
    public ModelRenderer shape11;
    public ModelRenderer shape12;
    public ModelRenderer shape13;
    public ModelRenderer shape14;
    public ModelRenderer shape15;
    public ModelRenderer shape15_1;
    public ModelRenderer shape5;
    public ModelRenderer shape9_1;
    public ModelRenderer shape10_1;
    public ModelRenderer shape1;
    public ModelRenderer shape2;
    public ModelRenderer shape3;
    public ModelRenderer shape4;
    public ModelRenderer shape8_1;
    public ModelRenderer shape11_1;
    public ModelRenderer shape12_1;
    public ModelRenderer shape14_1;
    public ModelRenderer shape16;
    public ModelRenderer shape13_1;
    public ModelRenderer shape17;
    public ModelRenderer shape18;
    public ModelRenderer shape21;
    public ModelRenderer shape20;
    public ModelRenderer shape22;
    public ModelRenderer shape23;
    public ModelRenderer shape25;
    public ModelRenderer shape24;
    public ModelRenderer shape19;
    public ModelRenderer Supressor;
    public ModelRenderer Supressor2;
    public ModelRenderer Supressor3;
    public ModelRenderer Supressor4;
    public ModelRenderer Magazine;
    public ModelRenderer Magazine2;

    public ModelZombieXmas()
    {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.Magazine = new ModelRenderer(this, 0, 55);
        this.Magazine.setRotationPoint(-6.599999999999997F, -0.10000000000000359F, -8.999999999999995F);
        this.Magazine.addBox(0.0F, -0.1F, 0.0F, 1, 8, 1, 0.0F);
        this.Supressor4 = new ModelRenderer(this, 0, 9);
        this.Supressor4.setRotationPoint(-6.399999999999997F, -1.7999999999999932F, -17.499999999999975F);
        this.Supressor4.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape14 = new ModelRenderer(this, 28, 44);
        this.shape14.setRotationPoint(2.0F, 0.5F, -0.5F);
        this.shape14.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape24 = new ModelRenderer(this, 8, 59);
        this.shape24.setRotationPoint(-6.999999999999996F, -3.969047313034935E-15F, -8.099999999999998F);
        this.shape24.addBox(0.0F, -0.1F, 0.0F, 1, 4, 1, 0.0F);
        this.bipedRightLeg = new ModelRenderer(this, 0, 16);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.1F);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.shape8_1 = new ModelRenderer(this, 7, 58);
        this.shape8_1.setRotationPoint(-7.099999999999995F, -3.969047313034935E-15F, -9.299999999999992F);
        this.shape8_1.addBox(0.0F, -0.1F, 0.0F, 2, 4, 2, 0.0F);
        this.bipedBody = new ModelRenderer(this, 16, 16);
        this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
        this.shape14_1 = new ModelRenderer(this, 0, 34);
        this.shape14_1.setRotationPoint(-6.599999999999997F, -2.3799999999999906F, -11.899999999999983F);
        this.shape14_1.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(shape14_1, 0.8028514559173915F, 0.0F, 0.0F);
        this.shape17 = new ModelRenderer(this, 0, 34);
        this.shape17.setRotationPoint(-6.4999999999999964F, -1.4999999999999958F, -12.44999999999998F);
        this.shape17.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape9_1 = new ModelRenderer(this, 8, 59);
        this.shape9_1.setRotationPoint(-6.1999999999999975F, -3.969047313034935E-15F, -8.099999999999998F);
        this.shape9_1.addBox(0.0F, -0.1F, 0.0F, 1, 4, 1, 0.0F);
        this.shape13 = new ModelRenderer(this, 28, 43);
        this.shape13.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.shape13.addBox(0.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape13, 0.0F, 0.0F, 0.8196066167365371F);
        this.Supressor = new ModelRenderer(this, 0, 9);
        this.Supressor.setRotationPoint(-6.799999999999996F, -1.7999999999999932F, -17.499999999999975F);
        this.Supressor.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape25 = new ModelRenderer(this, 19, 34);
        this.shape25.setRotationPoint(-6.599999999999997F, -2.4099999999999904F, -11.599999999999984F);
        this.shape25.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape15 = new ModelRenderer(this, 35, 58);
        this.shape15.setRotationPoint(0.7F, 0.6F, 0.0F);
        this.shape15.addBox(0.0F, -1.0F, -0.5F, 2, 2, 2, 0.0F);
        this.setRotateAngle(shape15, 0.0F, 0.0F, 0.5009094953223726F);
        this.shape21 = new ModelRenderer(this, 0, 34);
        this.shape21.setRotationPoint(-6.599999999999997F, -3.0999999999999863F, -6.199999999999998F);
        this.shape21.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape10_1 = new ModelRenderer(this, 9, 59);
        this.shape10_1.setRotationPoint(-6.599999999999997F, 0.9999999999999888F, -7.799999999999997F);
        this.shape10_1.addBox(0.0F, -0.1F, 0.0F, 1, 3, 1, 0.0F);
        this.shape22 = new ModelRenderer(this, 0, 34);
        this.shape22.setRotationPoint(-6.599999999999997F, -3.0999999999999863F, -6.299999999999997F);
        this.shape22.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.Supressor2 = new ModelRenderer(this, 0, 9);
        this.Supressor2.setRotationPoint(-6.399999999999997F, -1.3999999999999975F, -17.499999999999975F);
        this.Supressor2.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape8 = new ModelRenderer(this, 24, 51);
        this.shape8.setRotationPoint(-5.0F, -9.0F, -5.0F);
        this.shape8.addBox(0.0F, 0.0F, 0.0F, 10, 3, 10, 0.0F);
        this.shape12_1 = new ModelRenderer(this, 0, 36);
        this.shape12_1.setRotationPoint(-6.599999999999997F, 0.9999999999999888F, -11.199999999999985F);
        this.shape12_1.addBox(0.0F, -0.1F, 0.0F, 1, 0, 3, 0.0F);
        this.shape1 = new ModelRenderer(this, 0, 34);
        this.shape1.setRotationPoint(-7.099999999999995F, -2.3999999999999937F, -12.19999999999998F);
        this.shape1.addBox(0.0F, -0.1F, 0.0F, 2, 2, 7, 0.0F);
        this.shape18 = new ModelRenderer(this, 0, 34);
        this.shape18.setRotationPoint(-6.699999999999997F, -1.6999999999999924F, -12.44999999999998F);
        this.shape18.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.1F);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F);
        this.bipedRightArm = new ModelRenderer(this, 40, 16);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(bipedRightArm, -1.5707963267948966F, 0.0F, 0.0F);
        this.shape11_1 = new ModelRenderer(this, 0, 40);
        this.shape11_1.setRotationPoint(-6.599999999999997F, -3.969047313034935E-15F, -11.199999999999985F);
        this.shape11_1.addBox(0.0F, -0.1F, 0.0F, 1, 1, 0, 0.0F);
        this.shape16 = new ModelRenderer(this, 9, 61);
        this.shape16.setRotationPoint(-6.599999999999997F, 0.3099999999999922F, -8.029999999999998F);
        this.shape16.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(shape16, 0.2617993877991494F, 0.0F, 0.0F);
        this.shape11 = new ModelRenderer(this, 28, 42);
        this.shape11.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape11.addBox(0.0F, 0.0F, -2.5F, 4, 3, 5, 0.0F);
        this.setRotateAngle(shape11, 0.0F, 0.0F, 0.7740535232594852F);
        this.shape12 = new ModelRenderer(this, 28, 42);
        this.shape12.setRotationPoint(4.0F, 0.0F, 0.0F);
        this.shape12.addBox(0.0F, 0.0F, -2.0F, 3, 2, 4, 0.0F);
        this.setRotateAngle(shape12, 0.0F, 0.0F, 0.22759093446006054F);
        this.shape2 = new ModelRenderer(this, 0, 34);
        this.shape2.setRotationPoint(-6.049999999999997F, -2.4499999999999895F, -11.199999999999985F);
        this.shape2.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.Magazine2 = new ModelRenderer(this, 0, 55);
        this.Magazine2.setRotationPoint(-6.599999999999997F, -0.10000000000000359F, -8.399999999999997F);
        this.Magazine2.addBox(0.0F, -0.1F, 0.0F, 1, 8, 1, 0.0F);
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.shape19 = new ModelRenderer(this, 0, 34);
        this.shape19.setRotationPoint(-6.4999999999999964F, -1.6999999999999924F, -12.44999999999998F);
        this.shape19.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape10 = new ModelRenderer(this, 28, 40);
        this.shape10.setRotationPoint(-4.0F, 0.0F, 0.0F);
        this.shape10.addBox(0.0F, 0.0F, -3.0F, 4, 4, 6, 0.0F);
        this.setRotateAngle(shape10, 0.0F, 0.0F, -0.6373942428283291F);
        this.shape20 = new ModelRenderer(this, 16, 61);
        this.shape20.setRotationPoint(-6.599999999999997F, -1.5999999999999959F, -13.699999999999976F);
        this.shape20.addBox(0.0F, -0.1F, 0.0F, 1, 1, 2, 0.0F);
        this.bipedLeftArm = new ModelRenderer(this, 40, 16);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(bipedLeftArm, 0.0F, 0.10000736613927509F, -0.10000736613927509F);
        this.Supressor3 = new ModelRenderer(this, 0, 9);
        this.Supressor3.setRotationPoint(-6.799999999999996F, -1.3999999999999975F, -17.499999999999975F);
        this.Supressor3.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape5 = new ModelRenderer(this, 0, 34);
        this.shape5.setRotationPoint(-7.199999999999995F, -0.9999999999999997F, -11.199999999999985F);
        this.shape5.addBox(0.0F, -0.1F, 0.0F, 2, 1, 5, 0.0F);
        this.shape13_1 = new ModelRenderer(this, 0, 34);
        this.shape13_1.setRotationPoint(-6.699999999999997F, -1.4999999999999958F, -12.44999999999998F);
        this.shape13_1.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape23 = new ModelRenderer(this, 0, 34);
        this.shape23.setRotationPoint(-6.049999999999997F, 2.899999999999977F, -8.699999999999996F);
        this.shape23.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape9 = new ModelRenderer(this, 24, 40);
        this.shape9.setRotationPoint(5.0F, -1.5F, 5.0F);
        this.shape9.addBox(-4.0F, 0.0F, -4.0F, 8, 2, 8, 0.0F);
        this.shape15_1 = new ModelRenderer(this, 0, 34);
        this.shape15_1.setRotationPoint(-6.599999999999997F, -3.0999999999999863F, -12.19999999999998F);
        this.shape15_1.addBox(0.0F, -0.1F, 0.0F, 1, 1, 1, 0.0F);
        this.shape4 = new ModelRenderer(this, 0, 34);
        this.shape4.setRotationPoint(-7.149999999999995F, -2.4499999999999895F, -11.199999999999985F);
        this.shape4.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape3 = new ModelRenderer(this, 0, 34);
        this.shape3.setRotationPoint(-5.999999999999997F, -0.9999999999999997F, -11.199999999999985F);
        this.shape3.addBox(0.0F, -0.1F, 0.0F, 1, 1, 5, 0.0F);
        this.shape13.addChild(this.shape14);
        this.shape12.addChild(this.shape13);
        this.shape14.addChild(this.shape15);
        this.shape10.addChild(this.shape11);
        this.shape11.addChild(this.shape12);
        this.shape9.addChild(this.shape10);
        this.shape8.addChild(this.shape9);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        GlStateManager.pushMatrix();
        if (entity instanceof EntityXmasZombieBoss)
        {
            GlStateManager.scale(1.9F, 1.9F, 1.9F);
            GlStateManager.translate(0.0F, -12.0F * scale, 0.0F);
            render(scale);
        }
        else
        {
            GlStateManager.scale(0.7F, 0.7F, 0.7F);
            GlStateManager.translate(0.0F, 10.0F * scale, 0.0F);
            render(scale);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;

        if (this.isRiding)
        {
            this.bipedRightArm.rotateAngleX += -((float) Math.PI / 5F);
            this.bipedLeftArm.rotateAngleX += -((float) Math.PI / 5F);
            this.bipedRightLeg.rotateAngleX = -1.4137167F;
            this.bipedRightLeg.rotateAngleY = ((float) Math.PI / 10F);
            this.bipedRightLeg.rotateAngleZ = 0.07853982F;
            this.bipedLeftArm.rotateAngleX = -1.4137167F;
            this.bipedLeftArm.rotateAngleY = -((float) Math.PI / 10F);
            this.bipedLeftArm.rotateAngleZ = -0.07853982F;
        }
    }

    public void render(float scale)
    {
        this.Magazine.render(scale);
        //this.Supressor4.render(scale);
        this.shape24.render(scale);
        this.bipedRightLeg.render(scale);
        this.shape8_1.render(scale);
        this.bipedBody.render(scale);
        this.shape14_1.render(scale);
        this.shape17.render(scale);
        this.shape9_1.render(scale);
        //this.Supressor.render(scale);
        this.shape25.render(scale);
        this.shape21.render(scale);
        this.shape10_1.render(scale);
        this.shape22.render(scale);
        //this.Supressor2.render(scale);
        this.shape8.render(scale);
        this.shape12_1.render(scale);
        this.shape1.render(scale);
        this.shape18.render(scale);
        this.bipedLeftLeg.render(scale);
        this.bipedRightArm.render(scale);
        this.shape11_1.render(scale);
        this.shape16.render(scale);
        this.shape2.render(scale);
        this.Magazine2.render(scale);
        this.bipedHead.render(scale);
        this.shape19.render(scale);
        this.shape20.render(scale);
        this.bipedLeftArm.render(scale);
        //this.Supressor3.render(scale);
        this.shape5.render(scale);
        this.shape13_1.render(scale);
        this.shape23.render(scale);
        this.shape15_1.render(scale);
        this.shape4.render(scale);
        this.shape3.render(scale);
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
