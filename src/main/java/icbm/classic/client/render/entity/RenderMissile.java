package icbm.classic.client.render.entity;

import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
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
    private RenderEntityItem2 renderEntityItem;

    public static RenderMissile INSTANCE;

    public RenderMissile(RenderManager renderManager)
    {
        super(renderManager);
        entityItem = new EntityItem(null);
        renderEntityItem = new RenderEntityItem2(renderManager, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks)
    {
        //Setup
        GlStateManager.pushMatrix();

        //Translate
        GlStateManager.translate(x, y, z);

        //Rotate
        float yaw = entityMissile.prevRotationYaw + (entityMissile.rotationYaw - entityMissile.prevRotationYaw) * partialTicks - 90;
        float pitch = entityMissile.prevRotationPitch + (entityMissile.rotationPitch - entityMissile.prevRotationPitch) * partialTicks - 90;
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 0.0F, 0.0F, 1.0F);

        //Render missile
        GlStateManager.translate(0, -1.5, 0);
        final float scale = 2;
        GlStateManager.scale(scale, scale, scale);
        renderMissile(entityMissile.explosiveID,
                entityMissile.world, entityMissile.posX, entityMissile.posY, entityMissile.posZ,
                0, 0, 0, entityYaw, partialTicks);

        //Reset
        GlStateManager.popMatrix();

        super.doRender(entityMissile, x, y, z, entityYaw, partialTicks);

        if (renderManager.isDebugBoundingBox()) //TODO fix so we can see motion vector
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            Vec3d vec3d = entityMissile.motionVector.toVec3d();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y, z).color(0, 255, 0, 255).endVertex();
            bufferbuilder.pos(x + vec3d.x * 2.0D, y + vec3d.y * 2.0D, z + vec3d.z * 2.0D).color(0, 255, 0, 2555).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMissile entity)
    {
        return null;
    }

    public void renderMissile(int explosiveID, World world, double wx, double wy, double wz,
                              double x, double y, double z, float entityYaw, float partialTicks)
    {
        renderMissile(new ItemStack(ItemReg.itemMissile, 1, explosiveID),
                world, wx, wy, wz,
                x, y, z, entityYaw, partialTicks);
    }

    public void renderMissile(ItemStack missileStack, TileEntity tileEntity,
                              double x, double y, double z, float entityYaw, float partialTicks)
    {
        renderMissile(missileStack,
                tileEntity.getWorld(),
                tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 0.5, tileEntity.getPos().getZ() + 0.5,
                x, y, z, entityYaw, partialTicks);
    }

    public void renderMissile(ItemStack missileStack, World world, double wx, double wy, double wz,
                              double x, double y, double z, float entityYaw, float partialTicks)
    {
        //Set data for fake entity
        entityItem.setWorld(world);
        entityItem.rotationYaw = 0;
        entityItem.setPosition(wx, wy, wz);
        entityItem.setItem(missileStack);

        //render entity item
        renderEntityItem.doRender(entityItem, x, y, z, entityYaw, partialTicks);
    }
}