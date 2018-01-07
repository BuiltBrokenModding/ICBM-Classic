package icbm.classic.content.explosive.tile;

import icbm.classic.content.explosive.Explosive;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBombBlock extends TileEntitySpecialRenderer<TileEntityExplosive>
{
    @Override
    public void render(TileEntityExplosive tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        Explosive explosive = tileEntity.explosive.handler;

        if (explosive != null && explosive.getBlockModel() != null && explosive.getBlockResource() != null)
        {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5f, y + 1.5f, z + 0.5f);
            GL11.glRotatef(180f, 0f, 0f, 1f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(explosive.getBlockResource());
            explosive.getBlockModel().render(0.0625f);
            GL11.glPopMatrix();
        }
    }
}
