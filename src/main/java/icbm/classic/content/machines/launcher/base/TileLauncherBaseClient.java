package icbm.classic.content.machines.launcher.base;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.*;
import icbm.classic.client.render.RenderMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
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
public class TileLauncherBaseClient extends TileLauncherBase implements ISimpleItemRenderer
{
    public static final ResourceLocation TEXTURE_FILE_0 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_0.png");
    public static final ResourceLocation TEXTURE_FILE_1 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_1.png");
    public static final ResourceLocation TEXTURE_FILE_2 = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "launcher_2.png");

    public static final MFaSheDi0 modelBase0 = new MFaSheDi0();
    public static final MFaSheDiRail0 modelRail0 = new MFaSheDiRail0();

    public static final MFaSheDi1 modelBase1 = new MFaSheDi1();
    public static final MFaSheDiRail1 modelRail1 = new MFaSheDiRail1();

    public static final MFaSheDi2 modelBase2 = new MFaSheDi2();
    public static final MFaSheDiRail2 modelRail2 = new MFaSheDiRail2();

    /** Client's render cached object, used in place of inventory to avoid affecting GUIs */
    public ItemStack cachedMissileStack;

    public TileLauncherBaseClient()
    {
        super();
        renderNormalBlock = false;
        renderTileEntity = true;
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherBaseClient();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float deltaFrame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 1.5F, (float) pos.z() + 0.5F);

        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        if (getDirection() != ForgeDirection.NORTH && getDirection() != ForgeDirection.SOUTH)
        {
            GL11.glRotatef(90F, 0F, 180F, 1.0F);
        }

        // The missile launcher screen
        if (getTier() == 0)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_0);
            modelBase0.render(0.0625F);
            modelRail0.render(0.0625F);
        }
        else if (getTier() == 1)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);
            modelBase1.render(0.0625F);
            modelRail1.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail1.render(0.0625F);
        }
        else if (getTier() == 2)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_2);
            modelBase2.render(0.0625F);
            modelRail2.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail2.render(0.0625F);
        }
        GL11.glPopMatrix();

        //TODO move to missile render class
        if (cachedMissileStack != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 0.5F, (float) pos.z() + 0.5F);

            //GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);


            Explosives e = Explosives.get(cachedMissileStack.getItemDamage());
            Explosion missile = e == null ? (Explosion) Explosives.CONDENSED.handler : (Explosion) e.handler;
            if (missile.missileModelPath != null && missile.missileModelPath.contains("missiles"))
            {
                GL11.glScalef(0.00625f, 0.00625f, 0.00625f);
            }
            else if (e != Explosives.NIGHTMARE)
            {
                GL11.glScalef(0.05f, 0.05f, 0.05f);
            }
            RenderMissile.renderMissile(missile);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        final int tier = itemStack.getItemDamage();

        GL11.glPushMatrix();
        GL11.glRotatef(180f, 0f, 0f, 1f);
        if (type == IItemRenderer.ItemRenderType.INVENTORY)
        {
            GL11.glTranslatef(0, -0.05f, 0);
            final float scale = 0.38f;
            GL11.glScalef(scale, scale, scale);
        }
        else if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
            final float scale = 0.5f;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(-0.7f, -2.5f, 0.7f);
            GL11.glRotatef(-90f, 0f, 1f, 0f);
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

        if (tier == 0)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_0);
            modelBase0.render(0.0625F);
            modelRail0.render(0.0625F);
        }
        else if (tier == 1)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_1);

            modelBase1.render(0.0625F);
            modelRail1.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail1.render(0.0625F);
        }
        else if (tier == 2)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_2);
            modelBase2.render(0.0625F);
            modelRail2.render(0.0625F);
            GL11.glRotatef(180F, 0F, 180F, 1.0F);
            modelRail2.render(0.0625F);
        }
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
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        this.tier = buf.readInt();
        if (buf.readBoolean())
        {
            cachedMissileStack = ByteBufUtils.readItemStack(buf);
        }
        else
        {
            cachedMissileStack = null;
        }
    }

    public ItemStack getMissileStack()
    {
        if (cachedMissileStack != null)
        {
            return cachedMissileStack;
        }
        return getStackInSlot(0);
    }

    @Override
    public IIcon getIcon()
    {
        return Blocks.anvil.getIcon(0, 0);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-2, 0, -2, 2, 4, 2).add(toPos()).toAABB();
    }
}
