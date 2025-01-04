package icbm.classic.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.ICBMConstants;
import icbm.classic.content.entity.AnvilEntity;
import icbm.classic.content.entity.EntityExplosiveFragment;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderAnvilBlock extends EntityRenderer<AnvilEntity>
{

    public RenderAnvilBlock(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(AnvilEntity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final BlockState blockState = Blocks.DAMAGED_ANVIL.getDefaultState()
            .with(AnvilBlock.FACING, Direction.Plane.HORIZONTAL.random(entity.world.rand));
        //TODO store rotation and damage in entity to reduce random nature


        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y + 0.5F, (float) z);


        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
        blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
        GlStateManager.translatef(0.0F, 0.0F, 1.0F);

        GlStateManager.popMatrix();


        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(AnvilEntity entity) {
        return null;
    }
}
