package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.CruiseLauncherTopModel;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.glu.Sphere;

public class TESREmpTower extends TileEntitySpecialRenderer<TileEntity>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();


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
        GlStateManager.translate((float) x + 0.5, (float) y + 0.5, (float) z + 0.5);

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blockRendererDispatcher.renderBlockBrightness(BlockEmpTower.COIL, 1f);
        GlStateManager.translate(0.0F, 0.0F, 1.0F);


        GlStateManager.enableLighting();

        GlStateManager.popMatrix();

        if(hasPower) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5, (float) y + 0.5, (float) z + 0.5);

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            renderBlock(blockRendererDispatcher, tile.getWorld(), tile.getPos(), BlockEmpTower.ELECTRIC);
            GlStateManager.translate(0.0F, 0.0F, 1.0F);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }

    private void renderBlock(BlockRendererDispatcher blockRendererDispatcher, World world, BlockPos pos, IBlockState state) {
        final BlockModelRenderer blockModelRenderer = blockRendererDispatcher.getBlockModelRenderer();
        final IBakedModel model = blockRendererDispatcher.getModelForState(state);
        final float brightness = 1f;

        int i = blockModelRenderer.blockColors.colorMultiplier(state, world, pos, 0);
        if (EntityRenderer.anaglyphEnable)
        {
            i = TextureUtil.anaglyphColor(i);
        }

        float red = (float)(i >> 16 & 255) / 255.0F;
        float green = (float)(i >> 8 & 255) / 255.0F;
        float blue = (float)(i & 255) / 255.0F;

        GlStateManager.color(brightness, brightness, brightness, 0.5F);
        blockModelRenderer.renderModelBrightnessColor(state, model, brightness, red, green, blue);
    }
}
