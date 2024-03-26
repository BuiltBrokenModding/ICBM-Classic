package icbm.classic.world.block.launcher.base;

import icbm.classic.client.render.entity.RenderMissile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.BlockEntitySpecialRenderer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherBase extends BlockEntitySpecialRenderer<LauncherBaseBlockEntity> {
    @Override
    public void render(LauncherBaseBlockEntity launcher, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        final float blockCenter = 0.5f;
        final float missileOffset = 2f;

        //Render missile
        if (!launcher.getMissileStack().isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                x + blockCenter + (launcher.getLaunchDirection().getFrontOffsetX() * missileOffset),
                y + blockCenter + (launcher.getLaunchDirection().getFrontOffsetY() * missileOffset),
                z + blockCenter + (launcher.getLaunchDirection().getFrontOffsetZ() * missileOffset)
            );
            GlStateManager.rotate(launcher.getMissileYaw(true), 0F, 1F, 0F);
            GlStateManager.rotate(launcher.getMissilePitch(true), 1F, 0F, 0F);

            RenderMissile.INSTANCE.renderMissile(launcher.getMissileStack(), launcher, 0, 0, 0, 0, 0);
            GlStateManager.popMatrix();
        }
    }
}
