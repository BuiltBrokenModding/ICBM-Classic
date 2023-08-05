package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityFlyingBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;

@SideOnly(Side.CLIENT)
public class RenderEntityBlock extends Render<EntityFlyingBlock>
{
    private final HashSet<IBlockState> failedBlocks = new HashSet();
    public RenderEntityBlock(RenderManager renderManager)
    {
        super(renderManager);
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityFlyingBlock entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        final IBlockState blockState = entity.getBlockState();
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);


        try {
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
            GlStateManager.translate(0.0F, 0.0F, 1.0F);
        }
        catch (Exception e) {
            if(!failedBlocks.contains(entity.getBlockState())) {
                failedBlocks.add(entity.getBlockState());

                // Locally change so user can still see the block
                entity.setBlockState(Blocks.STONE.getDefaultState());

                // Log issue, user will likely never notice but still worth logging
                ICBMClassic.logger().error("Unexpected error render flying block. This is likely an issue with another mod not ICBM!" + entity, e);
            }
        }

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityFlyingBlock entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}