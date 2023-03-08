package icbm.classic.client.render.entity.layer;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelChickenHelmet;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;

public class LayerChickenHelmet implements LayerRenderer<EntityChicken> {

    final ModelChickenHelmet helmet = new ModelChickenHelmet();
    final ResourceLocation texture = new ResourceLocation(ICBMConstants.DOMAIN, "textures/entity/space_chicken.png");

    private final RenderChicken renderChicken;

    public LayerChickenHelmet(RenderChicken renderChicken) {
        this.renderChicken = renderChicken;
    }

    @Override
    public void doRenderLayer(EntityChicken entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if(entity.hasCapability(CapSpaceChicken.INSTANCE, null)) {

            final CapSpaceChicken cap = entity.getCapability(CapSpaceChicken.INSTANCE, null);
            if(cap != null && cap.isSpace()) {

                renderChicken.bindTexture(texture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.alphaFunc(516, 0.003921569F);

                helmet.isChild = entity.isChild();
                helmet.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
