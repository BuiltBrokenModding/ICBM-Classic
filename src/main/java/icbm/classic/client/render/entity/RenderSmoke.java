package icbm.classic.client.render.entity;

import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.entity.EntitySmoke;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSmoke extends Render<EntitySmoke>
{
    public RenderSmoke(RenderManager renderManager)
    {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntitySmoke seat, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(seat, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySmoke entity)
    {
        return null;
    }
}