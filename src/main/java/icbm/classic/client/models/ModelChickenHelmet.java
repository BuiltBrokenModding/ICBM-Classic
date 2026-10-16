package icbm.classic.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icbm.classic.ICBMConstants;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

// Made with Blockbench 4.6.4
// PORT: rebuilt as a 1.21 layer definition, same box, pivot and texture size
public class ModelChickenHelmet extends EntityModel<Chicken> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "chicken_helmet"), "main");

    private final ModelPart bone;

    public ModelChickenHelmet(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createLayer() {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bone",
            CubeListBuilder.create().texOffs(40, 12).addBox(-3.0F, -6.5F, -4.5F, 6, 7, 6),
            PartPose.offset(0.0F, 15.0F, -4.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Chicken entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        bone.xRot = headPitch * 0.017453292F;
        bone.yRot = netHeadYaw * 0.017453292F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (this.young) {
            poseStack.pushPose();
            // 1.12 translate(0, 5 * scale, 2 * scale) with scale = 1/16
            poseStack.translate(0.0F, 5.0F / 16.0F, 2.0F / 16.0F);
            bone.render(poseStack, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        } else {
            bone.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
// PORTED
