package icbm.classic.content.radarstation;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelRadarStation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class TileRadarStationClient extends TileRadarStation implements ISimpleItemRenderer
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "radar.png");
    public static final ResourceLocation TEXTURE_FILE_OFF = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "radar_off.png");

    public static final ModelRadarStation MODEL = new ModelRadarStation();

    @Override
    public Tile newTile()
    {
        return new TileRadarStationClient();
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        GL11.glRotatef(180f, 0, 1, 0);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        MODEL.render(0.0625f, 0, 1.2f);
        GL11.glPopMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xf() + 0.5f, pos.yf() + 1.5f, pos.zf() + 0.5f);

        if (hasPower())
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        }
        else
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_OFF);
        }

        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        switch (getDirection().ordinal())
        {
            case 2:
                GL11.glRotatef(180F, 0.0F, 180F, 1.0F);
                break;
            case 4:
                GL11.glRotatef(90F, 0.0F, 180F, 1.0F);
                break;
            case 5:
                GL11.glRotatef(-90F, 0.0F, 180F, 1.0F);
                break;
        }

        MODEL.render(0.0625f, 0f, rotation);
        GL11.glPopMatrix();
    }
}
