package icbm.classic.content.blocks.emptower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renders the spinning coil of the EMP tower and its extenders. Client only.
 * <p>
 * PORT: register for both BlockReg.TILE_EMP_TOWER and BlockReg.TILE_EMP_TOWER_FAKE. Transforms are the
 * 1.12 GL calls applied to the pose stack. 1.12 rendered ELECTRIC with alpha 0.5, the translucent pass now
 * uses the texture's alpha.
 */
public class TESREmpTower<T extends BlockEntity> implements BlockEntityRenderer<T>
{
    public TESREmpTower(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        final BlockRenderDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRenderer();

        float rotation = 0;
        float prevRotation = 0;
        boolean hasPower = false;

        if(tile instanceof TileEMPTower) {
          rotation = ((TileEMPTower) tile).rotation;
          prevRotation = ((TileEMPTower) tile).prevRotation;
          hasPower = ((TileEMPTower) tile).getChargePercentage() > 0.2;
        }
        else if(tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null) {
            rotation = ((TileEmpTowerFake) tile).getHost().rotation;
            prevRotation = ((TileEmpTowerFake) tile).getHost().prevRotation;
            hasPower =  ((TileEmpTowerFake) tile).getHost().getChargePercentage() > 0.2;

            int height = tile.getBlockPos().getY() - ((TileEmpTowerFake) tile).getHost().getBlockPos().getY();
            if(height % 2 == 1) {
                rotation += 45;
                prevRotation += 45;
            }
        }
        rotation = prevRotation + (rotation - prevRotation) * partialTicks;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        // PORT: 1.12 renderBlockBrightness rotated the model 90 degrees on Y internally, renderSingleBlock does not (same as vanilla TntRenderer)
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        blockRendererDispatcher.renderSingleBlock(BlockEmpTower.COIL, poseStack, buffer, packedLight, packedOverlay, ModelData.EMPTY, RenderType.solid());
        poseStack.popPose();

        if(hasPower) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            renderBlock(blockRendererDispatcher, tile, BlockEmpTower.ELECTRIC, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderBlock(BlockRenderDispatcher blockRendererDispatcher, BlockEntity tile, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        final ModelBlockRenderer blockModelRenderer = blockRendererDispatcher.getModelRenderer();
        final BakedModel model = blockRendererDispatcher.getBlockModel(state);

        int i = Minecraft.getInstance().getBlockColors().getColor(state, tile.getLevel(), tile.getBlockPos(), 0);
        if (i == -1)
        {
            i = 0xFFFFFF;
        }

        float red = (float)(i >> 16 & 255) / 255.0F;
        float green = (float)(i >> 8 & 255) / 255.0F;
        float blue = (float)(i & 255) / 255.0F;

        blockModelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.translucent()), state, model, red, green, blue, packedLight, packedOverlay, ModelData.EMPTY, RenderType.translucent());
    }

    @Override
    public boolean shouldRenderOffScreen(T tile)
    {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(T tile)
    {
        // 1.12 TileEMPTower used INFINITE_EXTENT_AABB
        return AABB.INFINITE; // PORT-CHECK: NeoForge 21.1 IBlockEntityRendererExtension#getRenderBoundingBox
    }
}
// PORTED
