package icbm.classic.content.machines.coordinator;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelMissileCoordinator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/10/2017.
 */
public class TileMissileCoordinatorClient extends TileMissileCoordinator implements ISimpleItemRenderer
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "missile_coordinator_off.png");
    public static final ResourceLocation TEXTURE_FILE_ON = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "missile_coordinator_on.png");
    public static final ModelMissileCoordinator MODEL = new ModelMissileCoordinator();
    private float lastSeePlayer = 0;
    private float lastFlicker = 0;

    public TileMissileCoordinatorClient()
    {
        super();
        this.renderNormalBlock = false;
        this.renderTileEntity = true;
    }

    @Override
    public Tile newTile()
    {
        return new TileMissileCoordinatorClient();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x() + 0.5F, (float) pos.y() + 1.5F, (float) pos.z() + 0.5F);

        int radius = 4;
        List players = world().getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord - radius, yCoord - radius, zCoord - radius, xCoord + radius, yCoord + radius, zCoord + radius));

        // CalclaviaRenderHelper.disableLighting();
        // CalclaviaRenderHelper.enableBlending();

        if (players.size() > 0)
        {
            if (this.lastSeePlayer > 20 * 3)
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_ON);
            }
            else
            {
                // Flicker for first 3 seconds when player comes near.
                this.lastSeePlayer += frame;

                if (Math.random() * 3 < this.lastSeePlayer / 3 && this.lastFlicker <= 0)
                {
                    this.lastFlicker = 8;
                    FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE_ON);
                }
                else
                {
                    FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
                }

                this.lastFlicker -= frame;
            }

        }
        else
        {
            this.lastSeePlayer = 0;
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        }

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

        // CalclaviaRenderHelper.disableBlending();
        // CalclaviaRenderHelper.enableLighting();

        MODEL.render(0, 0.0625F);
        GL11.glPopMatrix();

    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.1f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        GL11.glRotatef(180f, 0f, 1f, 0f);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
        MODEL.render(0, 0.0625F);
        GL11.glPopMatrix();
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiMissileCoordinator(player, this);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return Blocks.anvil.getIcon(0, 0);
    }
}
