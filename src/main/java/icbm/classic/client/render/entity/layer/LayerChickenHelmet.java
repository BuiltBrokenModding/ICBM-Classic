package icbm.classic.client.render.entity.layer;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelChickenHelmet;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.ChickenModel;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.ResourceLocation;

public class LayerChickenHelmet extends LayerRenderer<ChickenEntity, ChickenModel<ChickenEntity>> {

    final ModelChickenHelmet helmet = new ModelChickenHelmet();
    final ResourceLocation texture = new ResourceLocation(ICBMConstants.DOMAIN, "textures/entity/space_chicken.png");


    public LayerChickenHelmet(IEntityRenderer<ChickenEntity, ChickenModel<ChickenEntity>> renderChicken) {
        super(renderChicken);
    }

    @Override
    public void render(ChickenEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {


        entity.getCapability(CapSpaceChicken.INSTANCE).ifPresent((cap) -> {
            if (cap.isSpace()) {

                bindTexture(texture);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.alphaFunc(516, 0.003921569F);

                helmet.setModelAttributes(getEntityModel());
                helmet.render(entity, netHeadYaw, headPitch, scale);

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
