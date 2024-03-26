package icbm.classic.world.block.emptower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.BlockEntitySpecialRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TESREmpTower extends BlockEntitySpecialRenderer<BlockEntity> {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();


        float rotation = 0;
        float prevRotation = 0;
        boolean hasPower = false;

        if (tile instanceof EmpTowerBlockEntity) {
            rotation = ((EmpTowerBlockEntity) tile).rotation;
            prevRotation = ((EmpTowerBlockEntity) tile).prevRotation;
            hasPower = ((EmpTowerBlockEntity) tile).getChargePercentage() > 0.2;
        } else if (tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null) {
            rotation = ((TileEmpTowerFake) tile).getHost().rotation;
            prevRotation = ((TileEmpTowerFake) tile).getHost().prevRotation;
            hasPower = ((TileEmpTowerFake) tile).getHost().getChargePercentage() > 0.2;

            int height = tile.getPos().getY() - ((TileEmpTowerFake) tile).getHost().getPos().getY();
            if (height % 2 == 1) {
                rotation += 45;
                prevRotation += 45;
            }
        }
        rotation = prevRotation + (rotation - prevRotation) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5, (float) y + 0.5, (float) z + 0.5);

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blockRendererDispatcher.renderBlockBrightness(EmpTowerBlock.COIL, 1f);
        GlStateManager.translate(0.0F, 0.0F, 1.0F);


        GlStateManager.enableLighting();

        GlStateManager.popMatrix();

        if (hasPower) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5, (float) y + 0.5, (float) z + 0.5);

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            renderBlock(blockRendererDispatcher, tile.getLevel(), tile.getPos(), EmpTowerBlock.ELECTRIC);
            GlStateManager.translate(0.0F, 0.0F, 1.0F);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }

    private void renderBlock(BlockRendererDispatcher blockRendererDispatcher, Level level, BlockPos pos, BlockState state) {
        final BlockModelRenderer blockModelRenderer = blockRendererDispatcher.getBlockModelRenderer();
        final IBakedModel model = blockRendererDispatcher.getModelForState(state);
        final float brightness = 1f;

        int i = blockModelRenderer.blockColors.colorMultiplier(state, world, pos, 0);
        if (EntityRenderer.anaglyphEnable) {
            i = TextureUtil.anaglyphColor(i);
        }

        float red = (float) (i >> 16 & 255) / 255.0F;
        float green = (float) (i >> 8 & 255) / 255.0F;
        float blue = (float) (i & 255) / 255.0F;

        GlStateManager.color(brightness, brightness, brightness, 0.5F);
        blockModelRenderer.renderModelBrightnessColor(state, model, brightness, red, green, blue);
    }
}
