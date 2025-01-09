package icbm.classic.client.models;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.passive.ChickenEntity;

// Made with Blockbench 4.6.4
// Exported for Minecraft version 1.7 - 1.12
public class ModelChickenHelmet extends EntityModel<ChickenEntity> {
    private final RendererModel bone;

    public ModelChickenHelmet() {
        textureWidth = 64;
        textureHeight = 32;

        bone = new RendererModel(this);
        bone.setRotationPoint(0.0F, 15.0F, -4.0F);
        bone.cubeList.add(new ModelBox(bone, 40, 12, -3.0F, -6.5F, -4.5F, 6, 7, 6, 0.0F, false));
    }

    public void render(ChickenEntity entityIn, float netHeadYaw, float headPitch, float scale) {

        bone.rotateAngleX = headPitch * 0.017453292F;
        bone.rotateAngleY = netHeadYaw * 0.017453292F;

        if (entityIn.isChild()) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 5.0F * scale, 2.0F * scale);
            bone.render(scale);
            GlStateManager.popMatrix();
        } else {
            bone.render(scale);
        }
    }
}