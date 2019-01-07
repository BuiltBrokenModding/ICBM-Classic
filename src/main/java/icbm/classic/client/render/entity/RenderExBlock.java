package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderExBlock extends Render<EntityExplosive>
{
    public RenderExBlock(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityExplosive entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GL11.glPushMatrix();
        renderBlock(entity, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
    }

    public void renderBlock(EntityExplosive entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final IBlockState blockState = ICBMClassic.blockExplosive.getDefaultState()
                .withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP) //TODO get direction from rotation
                .withProperty(BlockExplosive.EX_PROP, ICBMClassicAPI.getExplosive(entity.explosiveID, false));
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blockrendererdispatcher.renderBlockBrightness(blockState, entity.getBrightness());
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityExplosive entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
