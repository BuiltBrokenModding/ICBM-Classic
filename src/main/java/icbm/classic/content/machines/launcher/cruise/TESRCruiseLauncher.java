package icbm.classic.content.machines.launcher.cruise;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MXiaoFaSheQi;
import icbm.classic.client.models.MXiaoFaSheQiJia;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.content.explosive.Explosives;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRCruiseLauncher extends TileEntitySpecialRenderer<TileCruiseLauncher>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "cruise_launcher.png");

    public static final MXiaoFaSheQi MODEL0 = new MXiaoFaSheQi();
    public static final MXiaoFaSheQiJia MODEL1 = new MXiaoFaSheQiJia();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileCruiseLauncher launcher, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        float yaw = (float) launcher.currentAim.yaw();
        float pitch = (float) launcher.currentAim.pitch();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        MODEL0.render(0.0625F);
        GL11.glRotatef(-yaw, 0F, 1F, 0F);
        GL11.glRotatef(-pitch, 1F, 0F, 0F);
        MODEL1.render(0.0625F);
        GL11.glPopMatrix();

        if (launcher.cachedMissileStack != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5F, (float) y + 1, (float) z + 0.5f);
            GL11.glRotatef(yaw, 0F, 1F, 0F);
            GL11.glRotatef(pitch - 90, 1F, 0F, 0F);

            Explosives e = Explosives.get(launcher.cachedMissileStack.getItemDamage());
            try
            {
                RenderMissile.INSTANCE.renderMissile(e, launcher, 0, 0, 0, 0, partialTicks);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            GL11.glPopMatrix();
        }
    }
}
