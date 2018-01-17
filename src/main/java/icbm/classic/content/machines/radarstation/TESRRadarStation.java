package icbm.classic.content.machines.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelRadarStation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class TESRRadarStation extends TileEntitySpecialRenderer<TileRadarStation>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "radar.png");
    public static final ResourceLocation TEXTURE_FILE_OFF = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "radar_off.png");

    public static final ModelRadarStation MODEL = new ModelRadarStation();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileRadarStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(te.xf() + 0.5f, te.yf() + 1.5f, te.zf() + 0.5f);

        if (te.hasPower())
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        }
        else
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_OFF);
        }

        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        switch (te.getRotation().ordinal())
        {
            case 3:
                GL11.glRotatef(180F, 0.0F, 180F, 1.0F);
                break;
            case 5:
                GL11.glRotatef(90F, 0.0F, 180F, 1.0F);
                break;
            case 4:
                GL11.glRotatef(-90F, 0.0F, 180F, 1.0F);
                break;
        }

        MODEL.render(0.0625f, 0f, te.rotation);
        GL11.glPopMatrix();
    }
}
