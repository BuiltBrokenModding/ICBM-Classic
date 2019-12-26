package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMConstants;
import icbm.classic.client.models.ModelRadarStation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class TESRRadarStation extends TileEntitySpecialRenderer<TileRadarStation>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "radar.png");
    public static final ResourceLocation TEXTURE_FILE_OFF = new ResourceLocation(ICBMConstants.DOMAIN, "textures/models/" + "radar_off.png");

    public static final ModelRadarStation MODEL = new ModelRadarStation();

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileRadarStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        //Fix techne translation and rotation
        GlStateManager.translate(x + 0.5F, y + 1.5F, z + 0.5F);
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        //Assign texture
        if (te.hasPower())
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        }
        else
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_OFF);
        }

        if(te.getRotation() == EnumFacing.NORTH)
        {
            GlStateManager.rotate(180F, 0.0F, 1F, 0);
        }
        else if(te.getRotation() == EnumFacing.WEST)
        {
            GlStateManager.rotate(90F, 0.0F, 1F, 0);
        }
        else if(te.getRotation() == EnumFacing.EAST)
        {
            GlStateManager.rotate(-90F, 0.0F, 1F, 0);
        }

        MODEL.render(0.0625f, 0f, te.rotation);
        GlStateManager.popMatrix();
    }
}
