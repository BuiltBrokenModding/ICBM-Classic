package icbm.classic.content.machines.launcher.frame;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MFaSheJia;
import io.netty.buffer.ByteBuf;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TileLauncherFrameClient extends TileLauncherFrame implements ISimpleItemRenderer
{
    public static final MFaSheJia MODEL = new MFaSheJia();
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_0.png");

    public TileLauncherFrameClient()
    {
        super();
        this.renderNormalBlock = false;
        this.renderTileEntity = true;
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherFrameClient();
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        this.setTier(buf.readInt());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 1.25F, (float) pos.z() + 0.5F);
        GL11.glScalef(1f, 0.85f, 1f);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        if (getDirection() != ForgeDirection.NORTH && getDirection() != ForgeDirection.SOUTH)
        {
            GL11.glRotatef(90F, 0.0F, 180F, 1.0F);
        }

        MODEL.render(0.0625F);

        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        GL11.glRotatef(180f, 0f, 0f, 1f);

        if (type == IItemRenderer.ItemRenderType.INVENTORY)
        {
            GL11.glTranslatef(0f, 0.05f, 0f);
            GL11.glScalef(0.8f, 0.38f, 0.8f);
        }
        else if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
            GL11.glTranslatef(-0.5f, -0.3f, 0.5f);
            GL11.glRotatef(20f, 0f, 1f, 0f);
        }
        else if (type == IItemRenderer.ItemRenderType.EQUIPPED)
        {
            final float scale = 0.5f;
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(-45f, 0f, 1f, 0f);
            GL11.glTranslatef(0f, -2.5f, 1.7f);
        }
        else if (type == IItemRenderer.ItemRenderType.ENTITY)
        {
            GL11.glTranslatef(0, -1, 0);
        }


        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        MODEL.render(0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list)
    {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
    }

    @Override
    public IIcon getIcon()
    {
        return Blocks.anvil.getIcon(0, 0);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-1, 0, -1, 1, 3, 1).add(toPos()).toAABB();
    }
}
