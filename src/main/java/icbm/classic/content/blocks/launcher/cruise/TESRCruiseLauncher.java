package icbm.classic.content.blocks.launcher.cruise;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.ICBMConstants;
import icbm.classic.client.models.CruiseLauncherTopModel;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
//import org.lwjgl.util.glu.Sphere;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/10/2017.
 */
public class TESRCruiseLauncher extends TileEntityRenderer<TileCruiseLauncher>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/cruise_launcher_top.png");

    public static final CruiseLauncherTopModel model = new CruiseLauncherTopModel();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(TileCruiseLauncher launcher, double x, double y, double z, float partialTicks, int destroyStage)
    {
        float yaw = (float) launcher.currentAim.yaw();
        float pitch = (float) launcher.currentAim.pitch();

        // Render top of launcher
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 2.5F, (float) z + 0.5F);
        this.bindTexture(TEXTURE_FILE);

        GlStateManager.rotatef(180F, 0.0F, 0.0F, 1.0F);
        model.render(0.0625f, -(float)Math.toRadians(yaw), -(float)Math.toRadians(pitch));
        GlStateManager.popMatrix();

        // Render held missile
        if (!launcher.cachedMissileStack.isEmpty()) {

            GlStateManager.pushMatrix();
            GlStateManager.translated(x + 0.5f, (float) y + 2, z + 0.5f);

            RenderMissile.INSTANCE.renderItem(launcher.cachedMissileStack,launcher.getWorld(), 0, 0, 0, yaw, pitch - 90, partialTicks);
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
                GlStateManager.translatef((float) x + 0.5f, (float) y + 3f, (float) z + 0.5f);
                bindTexture(RenderRedmatter.GREY_TEXTURE);
                GlStateManager.color4f(0.0F, 0.0F, 1F, 1);
                //new Sphere().draw(0.5f, 32, 32);
                GlStateManager.popMatrix();

                if (!launcher.cachedMissileStack.isEmpty()) {
                    EulerAngle angle = new EulerAngle(yu, pu);
                    double offsetX = radius * angle.x();
                    double offsetZ = radius * angle.z();
                    double offsetY = radius * angle.y();

                    GlStateManager.pushMatrix();
                    GlStateManager.translated(x + 0.5f + offsetX, (float) y + 3f + offsetY, z + 0.5f + offsetZ);
                    bindTexture(RenderRedmatter.GREY_TEXTURE);
                    GlStateManager.color4f(0.0F, 0.0F, 1F, 1);
                    //new Sphere().draw(0.1f, 32, 32);
                    GlStateManager.popMatrix();


                    offsetX = (radius + 0.2) * angle.x();
                    offsetZ = (radius + 0.2) * angle.z();
                    offsetY = (radius + 0.2) * angle.y();

                    GlStateManager.pushMatrix();
                    GlStateManager.translated(x + 0.5 + offsetX, (float) y + 3 + offsetY, z + 0.5 + offsetZ);
                    GlStateManager.rotatef(yu, 0F, 1F, 0F);
                    GlStateManager.rotatef(pu + 90, 1F, 0F, 0F);

                    GlStateManager.translatef(0, -1, 0);
                    RenderMissile.INSTANCE.renderItem(launcher.cachedMissileStack,launcher.getWorld(),0, 0, 0, 0, 0, partialTicks);
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
