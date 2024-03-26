package icbm.classic.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import icbm.classic.IcbmConstants;
import icbm.classic.client.models.ModelChickenHelmet;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import org.jetbrains.annotations.NotNull;

public class LayerChickenHelmet extends RenderLayer<Chicken, ChickenModel<Chicken>> {

    final ModelChickenHelmet helmet = new ModelChickenHelmet();
    final ResourceLocation texture = new ResourceLocation(IcbmConstants.MOD_ID, "textures/entity/space_chicken.png");

    private final ChickenRenderer renderer;

    public LayerChickenHelmet(ChickenRenderer renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    protected @NotNull ResourceLocation getTextureLocation(@NotNull Chicken entity) {
        return texture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Chicken entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                       float headPitch) {
        CapSpaceChicken capability = entity.getCapability(CapSpaceChicken.INSTANCE);
        if (capability != null && capability.isSpace()) {
            // FIXME: Figure out how to render the helmet
//            renderer.bindTexture(texture);
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//            GlStateManager.pushMatrix();
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            GlStateManager.alphaFunc(516, 0.003921569F);
//
//            helmet.isChild = entity.isChild();
//            helmet.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//
//            GlStateManager.disableBlend();
//            GlStateManager.popMatrix();
        }
    }
}
