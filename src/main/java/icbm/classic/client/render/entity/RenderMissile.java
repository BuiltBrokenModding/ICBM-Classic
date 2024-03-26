package icbm.classic.client.render.entity;

import icbm.classic.world.missile.entity.EntityMissile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
/** Handles missile rendering for all object types. This
 * includes entity, item, inventory, and tiles.
 *
 * @author Calclavia, DarkCow
 * */
public class RenderMissile extends Render<EntityMissile> {

    private final ItemEntity entityItem;
    private final RenderItemEntity2 renderItemEntity;

    public static RenderMissile INSTANCE;

    public RenderMissile(RenderManager renderManager) {
        super(renderManager);
        entityItem = new ItemEntity(null);
        renderItemEntity = new RenderItemEntity2(renderManager, Minecraft.getMinecraft().getRenderItem(), ItemCameraTransforms.TransformType.NONE);
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks) {
        //Setup
        GlStateManager.pushMatrix();

        //Translate to center of entity collider
        GlStateManager.translate(x, y + 0.2, z);

        //Rotate
        float yaw = entityMissile.prevRotationYaw + (entityMissile.getYRot() - entityMissile.prevRotationYaw) * partialTicks - 180;
        float pitch = entityMissile.prevRotationPitch + (entityMissile.getXRot() - entityMissile.prevRotationPitch) * partialTicks - 90;
        GlStateManager.rotate(yaw, 0F, 1F, 0F);
        GlStateManager.rotate(pitch, 1F, 0F, 0F);

        //Translate to rotation point of model TODO extract from model file
        GlStateManager.translate(0, -0.8, 0);

        //Render missile
        renderMissile(entityMissile.toStack(),
            entityMissile.world, entityMissile.getX(), entityMissile.getY(), entityMissile.getZ(),
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
    protected ResourceLocation getEntityTexture(EntityMissile entity) {
        return null;
    }

    public void renderMissile(ItemStack missileStack, BlockEntity blockEntityEntity,
                              double x, double y, double z, float entityYaw, float partialTicks) {
        renderMissile(missileStack,
            tileEntity.getLevel(),
            tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 0.5, tileEntity.getPos().getZ() + 0.5,
            x, y, z, entityYaw, partialTicks);
    }

    public void renderMissile(ItemStack missileStack, Level level, double wx, double wy, double wz,
                              double x, double y, double z, float entityYaw, float partialTicks) {
        //Set data for fake entity
        entityItem.setLevel(world);
        entityItem.getYRot() = 0;
        entityItem.setPosition(wx, wy, wz);
        entityItem.setItem(missileStack);

        //render entity item
        renderItemEntity.doRender(entityItem, x, y, z, entityYaw, partialTicks);
    }
}