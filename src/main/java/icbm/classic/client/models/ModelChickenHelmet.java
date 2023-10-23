package icbm.classic.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

// Made with Blockbench 4.6.4
// Exported for Minecraft version 1.7 - 1.12
public class ModelChickenHelmet extends ModelBase {
    private final ModelRenderer bone;

    public ModelChickenHelmet() {
        textureWidth = 64;
        textureHeight = 32;

        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, 15.0F, -4.0F);
        bone.cubeList.add(new ModelBox(bone, 40, 12, -3.0F, -6.5F, -4.5F, 6, 7, 6, 0.0F, false));
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        bone.rotateAngleX = headPitch * 0.017453292F;
        bone.rotateAngleY = netHeadYaw * 0.017453292F;

        if (this.isChild) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 5.0F * scale, 2.0F * scale);
            bone.render(scale);
            GlStateManager.popMatrix();
        } else {
            bone.render(scale);
        }
    }
}