package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelTier1LauncherScreen;
import icbm.classic.client.models.ModelTier2LauncherScreen;
import icbm.classic.client.models.ModelTier3LauncherScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherScreen extends TileEntitySpecialRenderer<TileLauncherScreen>
{
    public static final ResourceLocation TEXTURE_FILE_0 = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "launcher_0.png");
    public static final ResourceLocation TEXTURE_FILE_1 = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "launcher_1.png");
    public static final ResourceLocation TEXTURE_FILE_2 = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "launcher_2.png");

    public static final ModelTier1LauncherScreen model0 = new ModelTier1LauncherScreen();
    public static final ModelTier2LauncherScreen model1 = new ModelTier2LauncherScreen();
    public static final ModelTier3LauncherScreen model2 = new ModelTier3LauncherScreen();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileLauncherScreen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        switch (te.getRotation().ordinal())
        {
            case 3:
                GlStateManager.rotate(180F, 0.0F, 180F, 1.0F);
                break;
            case 5:
                GlStateManager.rotate(90F, 0.0F, 180F, 1.0F);
                break;
            case 4:
                GlStateManager.rotate(-90F, 0.0F, 180F, 1.0F);
                break;
        }

        switch (te.getTier())
        {
            case TWO:
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);
                model1.render(0.0625F);
                break;
            case THREE:
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_2);
                model2.render(0.0625F);
                break;
            default:
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_0);
                model0.render(0.0625F);
                break;
        }
        GlStateManager.popMatrix();
    }

}
