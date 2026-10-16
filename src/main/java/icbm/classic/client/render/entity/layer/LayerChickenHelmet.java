package icbm.classic.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelChickenHelmet;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

public class LayerChickenHelmet extends RenderLayer<Chicken, ChickenModel<Chicken>> {

    final ModelChickenHelmet helmet;
    final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "textures/entity/space_chicken.png");

    public LayerChickenHelmet(RenderLayerParent<Chicken, ChickenModel<Chicken>> renderChicken, EntityModelSet models) {
        super(renderChicken);
        this.helmet = new ModelChickenHelmet(models.bakeLayer(ModelChickenHelmet.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Chicken entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(CapSpaceChicken.isSpace(entity)) {
            // PORT: 1.12 enabled alpha blending by hand, entity translucent render type does the same
            helmet.young = entity.isBaby();
            helmet.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            final VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
            helmet.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
        }
    }
}
// PORTED
