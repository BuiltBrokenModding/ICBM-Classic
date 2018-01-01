package icbm.classic.client.render.entity;

import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderSeat extends Render
{
    public RenderSeat()
    {
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {

    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return null;
    }
}