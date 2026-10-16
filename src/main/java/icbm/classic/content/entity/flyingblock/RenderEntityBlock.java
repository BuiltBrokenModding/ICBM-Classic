package icbm.classic.content.entity.flyingblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icbm.classic.ICBMClassic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

public class RenderEntityBlock extends EntityRenderer<EntityFlyingBlock>
{
    private final HashSet<BlockState> failedBlocks = new HashSet();

    public RenderEntityBlock(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(EntityFlyingBlock entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        BlockState blockState = entity.getBlockData().getBlockState();
        final BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushPose();
        poseStack.translate(0, 0.5F, 0);

        // If we previously failed try to use another state
        if (failedBlocks.contains(blockState))
        {
            if (blockState.is(BlockTags.LEAVES))
            {
                blockState = Blocks.OAK_LEAVES.defaultBlockState();
            }
            else
            {
                blockState = Blocks.STONE.defaultBlockState();
            }
        }

        try
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.translate(-0.5F, -0.5F, 0.5F);
            // PORT: 1.12 renderBlockBrightness rotated +90 on Y internally, renderSingleBlock does not (same as vanilla TntRenderer)
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            // 1.12 renderBlockBrightness used the entity brightness, packedLight is the 1.21 equivalent
            blockrendererdispatcher.renderSingleBlock(blockState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY); //TODO consider using item render instead?
        }
        catch (Exception e)
        {
            if (!failedBlocks.contains(blockState))
            {
                failedBlocks.add(blockState);

                // Log issue, user will likely never notice but still worth logging
                ICBMClassic.logger().error("Failed to render FlyingBlocks. This is likely an issue with the block being rendered. Please report the problem to the block's author."
                        + "\n Entity: " + entity
                        + "\n Block: " + entity.getBlockData().getBlockState()
                    , e);
            }
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFlyingBlock entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
// PORTED
