package icbm.classic.client.render.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
/** Handles missile rendering for all object types. This
 * includes entity, item, inventory, and tiles.
 *
 * @author Calclavia, DarkCow
 * */
public class RenderMissile extends Render<EntityMissile>
{
    private EntityItem entityItem;
    private RenderEntityItem renderEntityItem;

    public static RenderMissile INSTANCE;

    public RenderMissile(RenderManager renderManager)
    {
        super(renderManager);
        entityItem = new EntityItem(null);
        renderEntityItem = new RenderEntityItem(renderManager, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks)
    {
        //Setup
        GlStateManager.pushMatrix();

        //Translate
        GlStateManager.translate(x, y, z);

        //Rotate
        GlStateManager.rotate(entityMissile.prevRotationYaw + (entityMissile.rotationYaw - entityMissile.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        float pitch = entityMissile.prevRotationPitch + (entityMissile.rotationPitch - entityMissile.prevRotationPitch) * partialTicks - 90;
        GlStateManager.rotate(pitch, 0.0F, 0.0F, 1.0F);

        //Render missile
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(4, 4, 4);
        renderMissile(entityMissile.explosiveID,
                entityMissile.world, entityMissile.posX, entityMissile.posY, entityMissile.posZ,
                0, 0, 0, entityYaw, partialTicks);

        //Reset
        GlStateManager.popMatrix();

        super.doRender(entityMissile, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMissile entity)
    {
        return null;
    }

    public void renderMissile(Explosives ex, TileEntity tileEntity,
                              double x, double y, double z, float entityYaw, float partialTicks) throws Exception
    {
        renderMissile(ex, tileEntity.getWorld(), tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 0.5, tileEntity.getPos().getZ() + 0.5,
                x, y, z, entityYaw, partialTicks);
    }

    public void renderMissile(Explosives ex, World world, double wx, double wy, double wz,
                              double x, double y, double z, float entityYaw, float partialTicks)
    {
        //Set data for fake entity
        entityItem.setWorld(world);
        entityItem.setPosition(wx, wy, wz);
        entityItem.setItem(new ItemStack(ICBMClassic.itemMissile, 1, ex.ordinal()));

        //render entity item
        renderEntityItem.doRender(entityItem, x, y, z, entityYaw, partialTicks);
    }
}