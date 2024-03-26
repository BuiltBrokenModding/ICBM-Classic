package icbm.classic.client.render.entity;

import icbm.classic.IcbmConstants;
import icbm.classic.world.entity.FragmentsEntity;
import net.minecraft.block.BlockAnvil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.core.Direction;
import net.minecraft.init.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderFragments extends Render<FragmentsEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IcbmConstants.MOD_ID, "textures/entity/fragments/fragment.png");

    public RenderFragments(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(FragmentsEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);
        if (entity.isAnvil) {
            final BlockState blockState = Blocks.ANVIL.getDefaultState()
                .withProperty(BlockAnvil.DAMAGE, entity.world.rand.nextInt(2))
                .withProperty(BlockAnvil.FACING, Direction.Plane.HORIZONTAL.facings()[entity.world.rand.nextInt(3)]);
            //TODO store rotation and damage in entity to reduce random nature


            final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);


            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.5F);
            blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
            GlStateManager.translate(0.0F, 0.0F, 1.0F);

            GlStateManager.popMatrix();
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(entity.prevRotationYaw + (entity.getYRot() - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(entity.prevRotationPitch + (entity.getXRot() - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            GlStateManager.enableRescaleNormal();
            float f9 = (float) entity.arrowShake - partialTicks;

            if (f9 > 0.0F) {
                float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
                GlStateManager.rotate(f10, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
            GlStateManager.translate(-4.0F, 0.0F, 0.0F);

            GlStateManager.glNormal3f(0.05625F, 0.0F, 0.0F);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
            bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
            bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
            bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
            tessellator.draw();

            GlStateManager.glNormal3f(-0.05625F, 0.0F, 0.0F);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
            bufferbuilder.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
            bufferbuilder.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
            bufferbuilder.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
            tessellator.draw();

            for (int j = 0; j < 4; ++j) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.glNormal3f(0.0F, 0.0F, 0.05625F);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(-8.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                bufferbuilder.pos(8.0D, -2.0D, 0.0D).tex(0.5D, 0.0D).endVertex();
                bufferbuilder.pos(8.0D, 2.0D, 0.0D).tex(0.5D, 0.15625D).endVertex();
                bufferbuilder.pos(-8.0D, 2.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
                tessellator.draw();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }


        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(FragmentsEntity entity) {
        return TEXTURE;
    }
}
