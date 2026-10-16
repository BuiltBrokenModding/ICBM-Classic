package icbm.classic.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icbm.classic.ICBMConstants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Top (rotating) part of the cruise launcher. Made with Blockbench 4.6.4.
 * <p>
 * PORT: register {@link #LAYER} with {@link #createLayer()} in EntityRenderersEvent.RegisterLayerDefinitions,
 * then construct with {@code context.bakeLayer(LAYER)}.
 */
public class CruiseLauncherTopModel extends ModelICBM {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "cruise_launcher_top"), "main");

	private final ModelPart yaw;
	private final ModelPart pitch;

	public CruiseLauncherTopModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
		this.yaw = root.getChild("yaw");
		this.pitch = yaw.getChild("pitch");
	}

    public static LayerDefinition createLayer()
    {
        final MeshDefinition mesh = new MeshDefinition();
        final PartDefinition root = mesh.getRoot();

        final PartDefinition yaw = root.addOrReplaceChild("yaw", CubeListBuilder.create()
            .texOffs(0, 56).addBox(-8.0F, -2.0F, -3.0F, 16, 2, 6),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        yaw.addOrReplaceChild("pitch", CubeListBuilder.create()
            .texOffs(0, 13).addBox(4.0F, -2.0F, -7.0F, 2, 5, 14)
            .texOffs(30, 13).addBox(-6.0F, -2.0F, -7.0F, 2, 5, 14)
            .texOffs(3, 0).addBox(-4.0F, 1.0F, -6.0F, 8, 2, 4)
            .texOffs(27, 0).addBox(-4.0F, 1.0F, 2.0F, 8, 2, 4),
            PartPose.offset(0.0F, -15.0F, 0.0F));

        final CubeListBuilder arm = CubeListBuilder.create()
            .texOffs(6, 33).addBox(6.0F, -17.0F, -3.0F, 2, 5, 6)
            .texOffs(2, 44).addBox(6.0F, -12.0F, -3.0F, 2, 10, 2)
            .texOffs(18, 44).addBox(6.0F, -12.0F, 1.0F, 2, 10, 2);
        yaw.addOrReplaceChild("arm1", arm, PartPose.offset(0.0F, 0.0F, 0.0F));
        yaw.addOrReplaceChild("arm2", arm, PartPose.offset(-14.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * 1.12 render(scale, yaw, pitch), scale 0.0625 is applied by ModelPart itself
     */
    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float rotYaw, float rotPitch)
    {
        yaw.yRot = rotYaw;
        pitch.xRot = rotPitch;

        yaw.render(poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color)
    {
        yaw.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
// PORTED
