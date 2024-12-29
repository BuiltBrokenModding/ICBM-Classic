package icbm.classic.content.blocks.emptower;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TESREmpTower extends TileEntityRenderer<TileEntity>
{
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
    {
        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();


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

            int height = tile.getPos().getY() - ((TileEmpTowerFake) tile).getHost().getPos().getY();
            if(height % 2 == 1) {
                rotation += 45;
                prevRotation += 45;
            }
        }
        rotation = prevRotation + (rotation - prevRotation) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);

        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        GlStateManager.rotatef(rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
        blockRendererDispatcher.renderBlockBrightness(BlockEmpTowerBase.COIL, 1f);
        GlStateManager.translatef(0.0F, 0.0F, 1.0F);


        GlStateManager.enableLighting();

        GlStateManager.popMatrix();

        if(hasPower) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);

            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            GlStateManager.rotatef(rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
            renderBlock(blockRendererDispatcher, tile.getWorld(), tile.getPos(), BlockEmpTowerBase.ELECTRIC);
            GlStateManager.translatef(0.0F, 0.0F, 1.0F);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }

    private void renderBlock(BlockRendererDispatcher blockRendererDispatcher, World world, BlockPos pos, BlockState state) {
        final BlockModelRenderer blockModelRenderer = blockRendererDispatcher.getBlockModelRenderer();
        final IBakedModel model = blockRendererDispatcher.getModelForState(state);
        final float brightness = 1f;

        int i = blockModelRenderer.blockColors.getColor(state, world, pos, 0);

        float red = (float)(i >> 16 & 255) / 255.0F;
        float green = (float)(i >> 8 & 255) / 255.0F;
        float blue = (float)(i & 255) / 255.0F;

        GlStateManager.color4f(brightness, brightness, brightness, 0.5F);
        blockModelRenderer.renderModelBrightnessColor(state, model, brightness, red, green, blue);
    }
}
