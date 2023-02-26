package icbm.classic.content.blocks.launcher.base;

import icbm.classic.client.render.entity.RenderMissile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherBase extends TileEntitySpecialRenderer<TileLauncherBase>
{
    @Override
    public void render(TileLauncherBase launcher, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(launcher, x, y, z, partialTicks, destroyStage, alpha);

        //Render missile
        if (!launcher.getMissileStack().isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5F, y + 0.4f + 1.5f, z + 0.5F);
            if (launcher.getRotation() == EnumFacing.NORTH || launcher.getRotation() == EnumFacing.SOUTH)
            {
                GlStateManager.rotate(90F, 0F, 1F, 0F);
            }

            RenderMissile.INSTANCE.renderMissile(launcher.getMissileStack(), launcher, 0, 0, 0, 0, 0);
            GlStateManager.popMatrix();
        }
    }
}
