package icbm.classic.client.render.entity;

import icbm.classic.content.missile.entity.EntityMissile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

    private final EntityItem entityItem;
    private final RenderEntityItem2 renderEntityItem;

    public static RenderMissile INSTANCE;

    public RenderMissile(RenderManager renderManager)
    {
        super(renderManager);
        entityItem = new EntityItem(null);
        renderEntityItem = new RenderEntityItem2(renderManager, Minecraft.getMinecraft().getRenderItem(), ItemCameraTransforms.TransformType.NONE);
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks)
    {
        //Setup
        GlStateManager.pushMatrix();

        //Translate to center of entity collider
        GlStateManager.translate(x, y + 0.2, z);

        //Rotate
        float yaw = entityMissile.prevRotationYaw + (entityMissile.rotationYaw - entityMissile.prevRotationYaw) * partialTicks - 180;
        float pitch = entityMissile.prevRotationPitch + (entityMissile.rotationPitch - entityMissile.prevRotationPitch) * partialTicks - 90;
        GlStateManager.rotate(yaw, 0F, 1F, 0F);
        GlStateManager.rotate(pitch, 1F, 0F, 0F);

        //Translate to rotation point of model TODO extract from model file
        GlStateManager.translate(0, -0.8, 0);

        //Render missile
        renderMissile(entityMissile.toStack(),
                entityMissile.world, entityMissile.posX, entityMissile.posY, entityMissile.posZ,
                0, 0, 0, entityYaw, partialTicks);

        //Reset
        GlStateManager.popMatrix();

        super.doRender(entityMissile, x, y, z, entityYaw, partialTicks);

        if (renderManager.isDebugBoundingBox()) //TODO fix so we can see motion vector
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y, z).color(0, 255, 0, 255).endVertex();
            bufferbuilder.pos(x + entityMissile.motionX * 2.0D, y + entityMissile.motionY * 2.0D, z + entityMissile.motionZ * 2.0D).color(0, 255, 0, 2555).endVertex();
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