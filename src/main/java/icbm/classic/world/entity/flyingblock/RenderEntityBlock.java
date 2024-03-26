package icbm.classic.world.entity.flyingblock;

import icbm.classic.ICBMClassic;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashSet;

@OnlyIn(Dist.CLIENT)
public class RenderEntityBlock extends Render<FlyingBlockEntity> {
    private final HashSet<BlockState> failedBlocks = new HashSet();

    public RenderEntityBlock(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(FlyingBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        BlockState blockState = entity.getBlockState();
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);

        // If we previously failed try to use another state
        if (failedBlocks.contains(blockState)) {
            if (blockState.getMaterial() == Material.LEAVES) {
                blockState = Blocks.LEAVES.getDefaultState();
            } else {
                blockState = Blocks.STONE.getDefaultState();
            }
        }

        try {
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
            GlStateManager.translate(0.0F, 0.0F, 1.0F);
        } catch (Exception e) {
            if (!failedBlocks.contains(blockState)) {
                failedBlocks.add(blockState);

                // Log issue, user will likely never notice but still worth logging
                ICBMClassic.logger().error("Failed to render FlyingBlocks. This is likely an issue with the block being rendered. Please report the problem to the block's author."
                        + "\n Entity: " + entity
                        + "\n Block: " + entity.getBlockState()
                    , e);
            }
        }

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(FlyingBlockEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}