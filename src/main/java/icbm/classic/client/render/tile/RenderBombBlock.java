package icbm.classic.client.render.tile;

import com.builtbroken.mc.lib.render.RenderUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.MDiLei;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.TileExplosive;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBombBlock extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "s-mine.png");
    public static final int ID = RenderingRegistry.getNextAvailableRenderId();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        if (modelID == ID)
        {
            if (metadata == Explosives.SMINE.ordinal())
            {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 1.5F, 0.0F);
                GL11.glRotatef(180f, 0f, 0f, 1f);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_FILE);
                MDiLei.INSTANCE.render(0.0625F);
                GL11.glPopMatrix();
            }
            else
            {
                try
                {

                    RenderUtility.renderNormalBlockAsItem(block, metadata, renderer);
                }
                catch (Exception e)
                {
                    ICBMClassic.INSTANCE.logger().error("ICBM Explosive Rendering Crash with: " + block + " and metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess iBlockAccess, int x, int y, int z, Block block, int modelID, RenderBlocks renderer)
    {
        if (modelID == ID)
        {
            TileEntity tileEntity = iBlockAccess.getTileEntity(x, y, z);

            if (tileEntity instanceof TileExplosive)
            {
                Explosive explosive = ((TileExplosive) tileEntity).haoMa.handler;

                if (!(explosive.getBlockModel() != null && explosive.getBlockResource() != null))
                {
                    renderer.renderStandardBlock(block, x, y, z);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId)
    {
        return true;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
    {
        if (tileEntity instanceof TileExplosive)
        {
            Explosive explosive = ((TileExplosive) tileEntity).haoMa.handler;

            if (explosive != null && explosive.getBlockModel() != null && explosive.getBlockResource() != null)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(x + 0.5f, y + 1.5f, z + 0.5f);
                GL11.glRotatef(180f, 0f, 0f, 1f);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(explosive.getBlockResource());
                explosive.getBlockModel().render(0.0625f);
                RenderUtility.setTerrainTexture();
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public int getRenderId()
    {
        return ID;
    }

}
