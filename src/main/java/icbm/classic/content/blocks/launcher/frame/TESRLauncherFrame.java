package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MFaSheJia;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherFrame extends TileEntitySpecialRenderer<TileLauncherFrame>
{
    public static final MFaSheJia MODEL = new MFaSheJia();
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_0.png");

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileLauncherFrame frame, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.25F, (float) z + 0.5F);
        GL11.glScalef(1f, 0.85f, 1f);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        if (frame.getRotation() != EnumFacing.NORTH && frame.getRotation() != EnumFacing.SOUTH)
        {
            GL11.glRotatef(90F, 0.0F, 180F, 1.0F);
        }

        MODEL.render(0.0625F);

        GL11.glPopMatrix();
    }
}
