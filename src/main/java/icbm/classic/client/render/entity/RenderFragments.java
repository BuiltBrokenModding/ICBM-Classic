package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import icbm.classic.ICBMConstants;
import icbm.classic.content.entity.EntityFragments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RenderFragments extends EntityRenderer<EntityFragments>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "textures/entity/fragments/fragment.png");

    /** 1.12 EnumFacing.Plane.HORIZONTAL.facings() order, only the first 3 were picked */
    private static final Direction[] ANVIL_FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH};

    public RenderFragments(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(EntityFragments entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        if (entity.isAnvil)
        {
            // PORT: 1.12 anvil DAMAGE property 0-1 became the anvil and chipped_anvil blocks
            final BlockState blockState = (entity.level().random.nextInt(2) == 0 ? Blocks.ANVIL : Blocks.CHIPPED_ANVIL).defaultBlockState()
                    .setValue(AnvilBlock.FACING, ANVIL_FACINGS[entity.level().random.nextInt(3)]);
            //TODO store rotation and damage in entity to reduce random nature

            final BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();

            poseStack.pushPose();
            poseStack.translate(0, 0.5F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.translate(-0.5F, -0.5F, 0.5F);
            // PORT: 1.12 renderBlockBrightness rotated +90 on Y internally, renderSingleBlock does not (see vanilla TntRenderer)
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            blockrendererdispatcher.renderSingleBlock(blockState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        else
        {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

            float f9 = (float) entity.arrowShake - partialTicks;
            if (f9 > 0.0F)
            {
                float f10 = -Mth.sin(f9 * 3.0F) * f9;
                poseStack.mulPose(Axis.ZP.rotationDegrees(f10));
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
            poseStack.scale(0.05625F, 0.05625F, 0.05625F);
            poseStack.translate(-4.0F, 0.0F, 0.0F);

            final VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
            PoseStack.Pose pose = poseStack.last();

            // Back faces
            vertex(pose, consumer, -7, -2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLight);
            vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLight);
            vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLight);
            vertex(pose, consumer, -7, 2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLight);

            vertex(pose, consumer, -7, 2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLight);
            vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLight);
            vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLight);
            vertex(pose, consumer, -7, -2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLight);

            // Shaft
            for (int j = 0; j < 4; ++j)
            {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose = poseStack.last();
                vertex(pose, consumer, -8, -2, 0, 0.0F, 0.0F, 0, 0, 1, packedLight);
                vertex(pose, consumer, 8, -2, 0, 0.5F, 0.0F, 0, 0, 1, packedLight);
                vertex(pose, consumer, 8, 2, 0, 0.5F, 0.15625F, 0, 0, 1, packedLight);
                vertex(pose, consumer, -8, 2, 0, 0.0F, 0.15625F, 0, 0, 1, packedLight);
            }

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, float nx, float ny, float nz, int packedLight)
    {
        consumer.addVertex(pose, x, y, z)
            .setColor(1f, 1f, 1f, 1f)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFragments entity)
    {
        return TEXTURE;
    }
}
// PORTED
