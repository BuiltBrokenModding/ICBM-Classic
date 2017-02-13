package icbm.classic.content.machines.launcher.screen;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MFaSheShiMuo0;
import icbm.classic.client.models.MFaSheShiMuo1;
import icbm.classic.client.models.MFaSheShiMuo2;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TileLauncherScreenClient extends TileLauncherScreen implements ISimpleItemRenderer
{
    public static final ResourceLocation TEXTURE_FILE_0 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_0.png");
    public static final ResourceLocation TEXTURE_FILE_1 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_1.png");
    public static final ResourceLocation TEXTURE_FILE_2 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_2.png");

    public static final MFaSheShiMuo0 model0 = new MFaSheShiMuo0();
    public static final MFaSheShiMuo1 model1 = new MFaSheShiMuo1();
    public static final MFaSheShiMuo2 model2 = new MFaSheShiMuo2();

    public TileLauncherScreenClient()
    {
        super();
        this.renderNormalBlock = false;
        this.renderTileEntity = true;
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherScreenClient();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 1.5F, (float) pos.z() + 0.5F);

        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        switch (getDirection().ordinal())
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

        switch (getTier())
        {
            case 1:
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);
                model1.render(0.0625F);
                break;
            case 2:
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

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        int tier = itemStack.getItemDamage();

        GL11.glTranslatef(0f, 0.9f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        GL11.glRotatef(180f, 0f, 180f, 1f);

        if (tier == 0)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_0);
            model0.render(0.0625F);
        }
        else if (tier == 1)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);
            model1.render(0.0625F);
        }
        else if (tier == 2)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_2);
            model2.render(0.0625F);
        }
        GL11.glPopMatrix();
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiLauncherScreen(this);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list)
    {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
    }
}
