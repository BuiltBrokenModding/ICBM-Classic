package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import icbm.classic.content.entity.EntityBombCart;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PORT: 1.12 used the vanilla TNT minecart render with the explosive block state as display tile. The explosive
 * is no longer a block state property, so the contents are drawn with the explosive's block model here, with the
 * vanilla TNT minecart swell and flash using the ICBM fuse.
 */
public class RenderBombCart extends MinecartRenderer<EntityBombCart>
{
    private final BlockRenderDispatcher blockRenderer;

    public RenderBombCart(EntityRendererProvider.Context context)
    {
        super(context, ModelLayers.TNT_MINECART);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    protected void renderMinecartContents(EntityBombCart entity, float partialTicks, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        final int fuse = entity.getBombFuse();
        if (fuse > -1 && (float) fuse - partialTicks + 1.0F < 10.0F)
        {
            float f = 1.0F - ((float) fuse - partialTicks + 1.0F) / 10.0F;
            f = Math.max(0.0F, Math.min(1.0F, f));
            f *= f;
            f *= f;
            final float scale = 1.0F + f * 0.3F;
            poseStack.scale(scale, scale, scale);
        }

        if (entity.getDisplayBlockState() == entity.getDefaultDisplayBlockState())
        {
            RenderExBlock.renderExplosiveBlock(blockRenderer, entity.explosive.getExplosiveData(), Direction.UP, poseStack, buffer, packedLight, fuse > -1 && fuse / 5 % 2 == 0);
        }
        else
        {
            // Custom display block set by command or another mod
            super.renderMinecartContents(entity, partialTicks, state, poseStack, buffer, packedLight);
        }
    }
}
// PORTED
