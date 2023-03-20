package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModuleCruiseLauncherBottom;
import icbm.classic.client.models.ModelCruiseLauncherTop;
import icbm.classic.client.render.entity.RenderLightBeam;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.rotation.Quaternion;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.glu.Sphere;

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

        if (!launcher.cachedMissileStack.isEmpty()) {

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5f, (float) y + 1, z + 0.5f);
            GlStateManager.rotate(yaw , 0F, 1F, 0F);
            GlStateManager.rotate(pitch - 90, 1F, 0F, 0F);

            try {
                RenderMissile.INSTANCE.renderMissile(launcher.cachedMissileStack, launcher, 0, 0, 0, 0, partialTicks);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            GlStateManager.popMatrix();
        }
    }

    public void debugMissileRotations(TileCruiseLauncher launcher, double x, double y, double z, float partialTicks)
    {
        final int missilesToRender = 8;

        for(int ry = 0; ry < missilesToRender; ry++) {
            for(int py = 0; py < missilesToRender; py++) {
                float yu = ry * (360f / missilesToRender);
                float pu = py * (360f / missilesToRender);
                float radius = 4f;

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) x + 0.5, (float) y + 3, (float) z + 0.5);
                bindTexture(RenderRedmatter.GREY_TEXTURE);
                GlStateManager.color(0.0F, 0.0F, 1F, 1);
                new Sphere().draw(0.5f, 32, 32);
                GlStateManager.popMatrix();

                if (!launcher.cachedMissileStack.isEmpty()) {
                    EulerAngle angle = new EulerAngle(yu, pu);
                    double offsetX = radius * angle.x();
                    double offsetZ = radius * angle.z();
                    double offsetY = radius * angle.y();

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 0.5 + offsetX, (float) y + 3 + offsetY, z + 0.5 + offsetZ);
                    bindTexture(RenderRedmatter.GREY_TEXTURE);
                    GlStateManager.color(0.0F, 0.0F, 1F, 1);
                    new Sphere().draw(0.1f, 32, 32);
                    GlStateManager.popMatrix();


                    offsetX = (radius + 0.2) * angle.x();
                    offsetZ = (radius + 0.2) * angle.z();
                    offsetY = (radius + 0.2) * angle.y();

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 0.5 + offsetX, (float) y + 3 + offsetY, z + 0.5 + offsetZ);
                    GlStateManager.rotate(yu, 0F, 1F, 0F);
                    GlStateManager.rotate(pu + 90, 1F, 0F, 0F);

                    GlStateManager.translate(0, -1, 0);

                    try {
                        RenderMissile.INSTANCE.renderMissile(launcher.cachedMissileStack, launcher, 0, 0, 0, 0, partialTicks);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
