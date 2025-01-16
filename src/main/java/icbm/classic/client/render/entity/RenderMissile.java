package icbm.classic.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.client.render.entity.item.RenderItemImp;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderMissile extends RenderItemImp<EntityMissile> {
    public static RenderMissile INSTANCE;

    public RenderMissile(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ItemStack getRenderItem(EntityMissile entity, int index) {
        if(index == 1 && entity instanceof EntityHeldItemMissile) {
            return ((EntityHeldItemMissile) entity).getItemStackHandler().getStackInSlot(0);
        }
        return entity.toStack();
    }

    @Override
    protected void translate(@Nullable EntityMissile entity, ItemStack itemstack, IBakedModel iBakedModel, double x, double y, double z, float partialTicks, int index) {
        //Translate to center of entity collider
        if (entity != null) {
            GlStateManager.translated(x, y + 0.2, z); //TODO handle in JSON
        } else {
            GlStateManager.translated(x, y, z);
        }
    }

    @Override
    protected float getYaw(@Nonnull EntityMissile entityMissile, float providedYaw, float partialTicks) {
        return providedYaw - 180; //TODO handle offset in JSON
    }

    @Override
    protected float getPitch(@Nonnull EntityMissile entityMissile, float providedPitch, float partialTicks) {
        return providedPitch - 90; //TODO handle offset in JSON
    }

    @Override
    protected void rotate(@Nullable EntityMissile entityMissile, ItemStack itemstack, float entityYaw, float entityPitch, float partialTicks, int index) {
        //Rotate
        if(index == 0) {
            GlStateManager.rotatef(entityYaw, 0F, 1F, 0F);
            GlStateManager.rotatef(entityPitch, 1F, 0F, 0F);
        }
        else if(index == 1) {
            GlStateManager.rotatef(entityYaw, 0F, 1F, 0F);
            GlStateManager.rotatef(entityPitch + 10, 1F, 0F, 0F);
            //GlStateManager.rotate(-45, 0F, 0F, 1F);
        }

        //Translate to rotation point of model TODO handle in JSON
        if(entityMissile != null) {
            if(index == 0) {
                GlStateManager.translated(0, -0.8, 0);
            }
            else if(index == 1) {
                GlStateManager.translated(0, 0.27, -0.12);
            }
        }
        else if(index == 1) {
            GlStateManager.translated(0, 1.1, -0.27);
        }
    }

    @Override
    protected ItemCameraTransforms.TransformType getTransformType(int index) {
        if(index == 1) {
            return ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemCameraTransforms.TransformType.NONE;
    }

    @Override
    protected void scale(@Nullable EntityMissile e, ItemStack itemstack, float partialTicks, int index) {
       if(index == 1 && !itemstack.getItem().isComplex()) { //TODO use a config driven list rather than assuming 3D means tool that can be held
           GlStateManager.scaled(0.5, 0.5, 0.5);
       }
    }

    @Override
    public void renderItem(ItemStack missileStack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks)
    {
        super.renderItem(missileStack, world, x, y, z, entityYaw, entityPitch, partialTicks);

        //TODO cache on caller for better render performance
        //TODO move to sub-renderer
        if(missileStack.getItem() == ItemReg.MISSILE_HELD_ITEM.get()) {
           final LazyOptional<ICapabilityMissileStack> cap = missileStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
           if(cap.isPresent() && cap.orElseThrow(IllegalStateException::new) instanceof CapabilityHeldItemMissile) {
               this.renderItem(null, ((CapabilityHeldItemMissile) cap.orElseThrow(IllegalStateException::new)).getHeldItem(), world, x, y, z, entityYaw, entityPitch, partialTicks, 1);
           }
        }
    }

    @Override
    protected void doRenderItem(@Nonnull EntityMissile entity, World world, double x, double y, double z, float yaw, float pitch, float partialTicks) {
        super.doRenderItem(entity, world, x, y, z, yaw, pitch, partialTicks);
        if(entity instanceof EntityHeldItemMissile) { //TODO move to sub-renderer
            this.renderItem(entity, getRenderItem(entity, 1), entity.world, x, y, z, yaw, pitch, partialTicks, 1);
        }
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entityMissile, x, y, z, entityYaw, partialTicks);

        if (renderManager.isDebugBoundingBox()) //TODO fix so we can see motion vector
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y, z).color(0, 255, 0, 255).endVertex();
            bufferbuilder.pos(
                    x + entityMissile.getMotion().x * 2.0D,
                    y + entityMissile.getMotion().y * 2.0D,
                    z + entityMissile.getMotion().z * 2.0D
                )
                .color(0, 255, 0, 2555)
                .endVertex();
            tessellator.draw();
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }
}