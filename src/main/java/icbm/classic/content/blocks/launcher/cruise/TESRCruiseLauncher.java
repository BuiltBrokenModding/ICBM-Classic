package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModuleCruiseLauncherBottom;
import icbm.classic.client.models.ModelCruiseLauncherTop;
import icbm.classic.client.render.entity.RenderMissile;
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
public class TESRCruiseLauncher extends TileEntitySpecialRenderer<TileCruiseLauncher>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "cruise_launcher.png");

    public static final ModuleCruiseLauncherBottom MODEL0 = new ModuleCruiseLauncherBottom();
    public static final ModelCruiseLauncherTop MODEL1 = new ModelCruiseLauncherTop();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileCruiseLauncher launcher, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        float yaw = (float) launcher.currentAim.yaw();
        float pitch = (float) launcher.currentAim.pitch();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        MODEL0.render(0.0625F);

        GlStateManager.rotate(-yaw, 0F, 1F, 0F); //TODO add lerp function to smooth rotation when FPS spikes
        GlStateManager.rotate(-pitch, 1F, 0F, 0F);
        MODEL1.render(0.0625F);

        GlStateManager.popMatrix();

        if (!launcher.cachedMissileStack.isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y + 1, (float) z + 0.5f);
            GlStateManager.rotate(yaw, 0F, 1F, 0F);
            GlStateManager.rotate(pitch - 90, 1F, 0F, 0F);

            try
            {
                RenderMissile.INSTANCE.renderMissile(launcher.cachedMissileStack, launcher, 0, 0, 0, 0, partialTicks);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            GlStateManager.popMatrix();
        }
    }
}
