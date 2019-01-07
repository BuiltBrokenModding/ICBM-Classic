package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MFaSheShiMuo0;
import icbm.classic.client.models.MFaSheShiMuo1;
import icbm.classic.client.models.MFaSheShiMuo2;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherScreen extends TileEntitySpecialRenderer<TileLauncherScreen>
{
    public static final ResourceLocation TEXTURE_FILE_0 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_0.png");
    public static final ResourceLocation TEXTURE_FILE_1 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_1.png");
    public static final ResourceLocation TEXTURE_FILE_2 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_2.png");

    public static final MFaSheShiMuo0 model0 = new MFaSheShiMuo0();
    public static final MFaSheShiMuo1 model1 = new MFaSheShiMuo1();
    public static final MFaSheShiMuo2 model2 = new MFaSheShiMuo2();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileLauncherScreen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

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
        GL11.glPopMatrix();
    }

}
