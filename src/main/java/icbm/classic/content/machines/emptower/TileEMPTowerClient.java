package icbm.classic.content.machines.emptower;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelEmpTower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TileEMPTowerClient extends TileEMPTower implements ISimpleItemRenderer
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "emp_tower.png");
    public static final ModelEmpTower MODEL = new ModelEmpTower();

    public TileEMPTowerClient()
    {
        super();
        this.renderNormalBlock = false;
        this.renderTileEntity = true;
    }

    @Override
    public Tile newTile()
    {
        return new TileEMPTowerClient();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 1.5F, (float) pos.z() + 0.5F);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        MODEL.render(rotation, 0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0.3f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        GL11.glScalef(0.6f, 0.6f, 0.6f);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);

        MODEL.render(0, 0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiEMPTower(this);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return Blocks.anvil.getIcon(0, 0);
    }
}
