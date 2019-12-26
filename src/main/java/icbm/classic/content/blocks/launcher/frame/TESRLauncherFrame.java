package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelLauncerFrame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherFrame extends TileEntitySpecialRenderer<TileLauncherFrame>
{
    public static final ModelLauncerFrame MODEL = new ModelLauncerFrame();
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "launcher_0.png");

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileLauncherFrame frame, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.25F, (float) z + 0.5F);
        GlStateManager.scale(1f, 0.85f, 1f);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        if (frame.getRotation() != EnumFacing.NORTH && frame.getRotation() != EnumFacing.SOUTH)
        {
            GlStateManager.rotate(90F, 0.0F, 180F, 1.0F);
        }

        MODEL.render(0.0625F);

        GlStateManager.popMatrix();
    }
}
