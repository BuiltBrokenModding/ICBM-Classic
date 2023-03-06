package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.CruiseLauncherTopModel;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
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
        final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        float rotation = 0;
        float prevRotation = 0;

        if(tile instanceof TileEMPTower) {
          rotation = ((TileEMPTower) tile).rotation;
          prevRotation = ((TileEMPTower) tile).prevRotation;
        }
        else if(tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null) {
            rotation = ((TileEmpTowerFake) tile).getHost().rotation;
            prevRotation = ((TileEmpTowerFake) tile).getHost().prevRotation;

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
        blockrendererdispatcher.renderBlockBrightness(BlockEmpTower.COIL, 1f);
        GlStateManager.translate(0.0F, 0.0F, 1.0F);


        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }
}
