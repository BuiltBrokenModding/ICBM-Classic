package icbm.classic.client.render.entity;

import icbm.classic.prefab.tile.IcbmBlock;
import icbm.classic.world.block.explosive.ExplosiveBlock;
import icbm.classic.world.entity.ExplosiveEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderExBlock extends Render<ExplosiveEntity> {
    public RenderExBlock(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(ExplosiveEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.pushMatrix();
        renderBlock(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }

    public void renderBlock(ExplosiveEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        final BlockState blockState = BlockReg.blockExplosive.getDefaultState()
            .withProperty(IcbmBlock.ROTATION_PROP, Direction.UP) //TODO get direction from rotation
            .withProperty(ExplosiveBlock.EX_PROP, entity.getExplosiveData());
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);

        //expansion shortly before explosion
        if ((float) entity.fuse - partialTicks + 1.0F < 10.0F) {
            float f = 1.0F - ((float) entity.fuse - partialTicks + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            float f1 = 1.0F + f * f * f * 0.3F;
            GlStateManager.scale(f1, f1, f1);
        }

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        //white flashing
        if (entity.fuse / 5 % 2 == 0) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, (1.0F - ((float) entity.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            blockrendererdispatcher.renderBlockBrightness(BlockReg.blockExplosive.getDefaultState(), 1.0F);
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ExplosiveEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
