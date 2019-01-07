package icbm.classic.content.machines.launcher.base;

import icbm.classic.ICBMClassic;
import icbm.classic.client.models.*;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.api.EnumTier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TESRLauncherBase extends TileEntitySpecialRenderer<TileLauncherBase>
{
    public static final ResourceLocation TEXTURE_FILE_0 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/launcher_0.png");
    public static final ResourceLocation TEXTURE_FILE_1 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/launcher_1.png");
    public static final ResourceLocation TEXTURE_FILE_2 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/launcher_2.png");

    public static final MFaSheDi0 modelBase0 = new MFaSheDi0();
    public static final MFaSheDiRail0 modelRail0 = new MFaSheDiRail0();

    public static final MFaSheDi1 modelBase1 = new MFaSheDi1();
    public static final MFaSheDiRail1 modelRail1 = new MFaSheDiRail1();

    public static final MFaSheDi2 modelBase2 = new MFaSheDi2();
    public static final MFaSheDiRail2 modelRail2 = new MFaSheDiRail2();

    @Override
    public void render(TileLauncherBase launcher, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(launcher, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1.5F, z + 0.5F);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        if (launcher.getRotation() != EnumFacing.NORTH && launcher.getRotation() != EnumFacing.SOUTH)
        {
            GlStateManager.rotate(90F, 0F, 180F, 1.0F);
        }

        // The missile launcher screen
        if (launcher.getTier() == EnumTier.ONE)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_0);
            modelBase0.render(0.0625F);
            modelRail0.render(0.0625F);
        }
        else if (launcher.getTier() == EnumTier.TWO)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);
            modelBase1.render(0.0625F);
            modelRail1.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail1.render(0.0625F);
        }
        else if (launcher.getTier() == EnumTier.THREE)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_2);
            modelBase2.render(0.0625F);
            modelRail2.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail2.render(0.0625F);
        }
        GlStateManager.popMatrix();

        //Render missile
        if (!launcher.getMissileStack().isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5F, y + 0.7f, z + 0.5F);
            if (launcher.getRotation() == EnumFacing.NORTH || launcher.getRotation() == EnumFacing.SOUTH)
            {
                GlStateManager.translate(0.05, 0, -0.1);
                GlStateManager.rotate(90F, 0F, 1F, 0F);
            }
            else
            {
                GlStateManager.translate(0.1, 0, 0.05);
            }
            GlStateManager.scale(2, 2, 2);


            Explosives e = Explosives.get(launcher.getMissileStack().getItemDamage());

            RenderMissile.INSTANCE.renderMissile(e, launcher, 0, 0, 0, 0, 0);
            GlStateManager.popMatrix();
        }
    }
}
