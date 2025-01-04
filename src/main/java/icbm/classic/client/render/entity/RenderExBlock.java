package icbm.classic.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderExBlock extends EntityRenderer<EntityExplosive>
{
    public RenderExBlock(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityExplosive entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.pushMatrix();
        renderBlock(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }

    public void renderBlock(EntityExplosive entity, double x, double y, double z, float entityYaw, float partialTicks)
    {

        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y + 0.5F, (float) z);

        //expansion shortly before explosion
        if ((float)entity.fuse - partialTicks + 1.0F < 10.0F)
        {
            float f = 1.0F - ((float)entity.fuse - partialTicks + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            float f1 = 1.0F + f * f * f * 0.3F;
            GlStateManager.scalef(f1, f1, f1);
        }

        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
        blockrendererdispatcher.renderBlockBrightness(entity.getBlockRender(), entity.getBrightness());
        GlStateManager.translatef(0.0F, 0.0F, 1.0F);

        //white flashing
        if (entity.fuse / 5 % 2 == 0)
        {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, (1.0F - ((float)entity.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F);
            GlStateManager.polygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            blockrendererdispatcher.renderBlockBrightness(entity.getBlockRender(), 1.0F);
            GlStateManager.polygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
        }

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityExplosive entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
