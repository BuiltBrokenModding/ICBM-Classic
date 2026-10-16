package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.client.render.entity.item.RenderItemImp;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderMissile extends RenderItemImp<EntityMissile>
{
    public static RenderMissile INSTANCE;

    public RenderMissile(EntityRendererProvider.Context context)
    {
        super(context);
        INSTANCE = this;
    }

    @Override
    protected ItemStack getRenderItem(EntityMissile entity, int index) {
        if(index == 1 && entity instanceof EntityHeldItemMissile) {
            return ((EntityHeldItemMissile) entity).getItemStackHandler().getStackInSlot(0);
        }
        return entity.toStack();
    }

    @Override
    protected void translate(@Nullable EntityMissile entity, ItemStack itemstack, BakedModel iBakedModel, PoseStack poseStack, float partialTicks, int index) {
        //Translate to center of entity collider
        if(entity != null) {
            poseStack.translate(0, 0.2, 0); //TODO handle in JSON
        }
    }

    @Override
    protected float getYaw(@Nonnull EntityMissile entityMissile, float providedYaw, float partialTicks) {
        return providedYaw - 180; //TODO handle offset in JSON
    }

    @Override
    protected float getPitch(@Nonnull EntityMissile entityMissile, float providedPitch, float partialTicks) {
        return  providedPitch - 90; //TODO handle offset in JSON
    }

    @Override
    protected void rotate(@Nullable EntityMissile entityMissile, ItemStack itemstack, PoseStack poseStack, float entityYaw, float entityPitch, float partialTicks, int index) {
        //Rotate
        if(index == 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch));
        }
        else if(index == 1) {
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch + 10));
        }

        //Translate to rotation point of model TODO handle in JSON
        if(entityMissile != null) {
            if(index == 0) {
                poseStack.translate(0, -0.8, 0);
            }
            else if(index == 1) {
                poseStack.translate(0, 0.27, -0.12);
            }
        }
        else if(index == 1) {
            poseStack.translate(0, 1.1, -0.27);
        }
    }

    @Override
    protected ItemDisplayContext getTransformType(int index) {
        if(index == 1) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemDisplayContext.NONE;
    }

    @Override
    protected void scale(@Nullable EntityMissile e, ItemStack itemstack, PoseStack poseStack, float partialTicks, int index) {
        // PORT-CHECK: 1.12 Item#isFull3D (tools, swords, sticks) approximated by the TOOL data component
        if(index == 1 && !itemstack.has(DataComponents.TOOL)) { //TODO use a config driven list rather than assuming 3D means tool that can be held
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    /**
     * Renders a missile stack outside of an entity, used by launcher block entity renderers.
     * Yaw and pitch are used as given (callers apply the model offsets, as in 1.12).
     *
     * @param x - offset from the current pose origin
     * @param y - offset from the current pose origin
     * @param z - offset from the current pose origin
     */
    public void renderItem(ItemStack missileStack, Level level, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           double x, double y, double z, float yaw, float pitch, float partialTicks)
    {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        this.renderItem(missileStack, level, poseStack, buffer, packedLight, yaw, pitch, partialTicks);
        poseStack.popPose();
    }

    @Override
    public void renderItem(ItemStack missileStack, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float entityYaw, float entityPitch, float partialTicks)
    {
        super.renderItem(missileStack, world, poseStack, buffer, packedLight, entityYaw, entityPitch, partialTicks);

        //TODO cache on caller for better render performance
        //TODO move to sub-renderer
        if(missileStack.getItem() == ItemReg.heldItemMissile.get()) {
           final ICapabilityMissileStack cap = missileStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
           if(cap instanceof CapabilityHeldItemMissile) {
               this.renderItem(null, ((CapabilityHeldItemMissile) cap).getHeldItem(), world, poseStack, buffer, packedLight, entityYaw, entityPitch, partialTicks, 1);
           }
        }
    }

    @Override
    protected void doRenderItem(@Nonnull EntityMissile entity, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float yaw, float pitch, float partialTicks) {
        super.doRenderItem(entity, world, poseStack, buffer, packedLight, yaw, pitch, partialTicks);
        if(entity instanceof EntityHeldItemMissile) { //TODO move to sub-renderer
            this.renderItem(entity, getRenderItem(entity, 1), entity.level(), poseStack, buffer, packedLight, yaw, pitch, partialTicks, 1);
        }
    }

    @Override
    public void render(@Nonnull EntityMissile entityMissile, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(entityMissile, entityYaw, partialTicks, poseStack, buffer, packedLight);

        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) //TODO fix so we can see motion vector
        {
            // PORT: 1.12 drew a raw GL line strip, now a line in the lines render type
            final Vec3 motion = entityMissile.getDeltaMovement();
            final Vec3 normal = motion.lengthSqr() > 0 ? motion.normalize() : new Vec3(0, 1, 0);
            final PoseStack.Pose pose = poseStack.last();
            final VertexConsumer lines = buffer.getBuffer(RenderType.lines());
            lines.addVertex(pose, 0, 0, 0).setColor(0, 255, 0, 255).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
            lines.addVertex(pose, (float) (motion.x * 2.0D), (float) (motion.y * 2.0D), (float) (motion.z * 2.0D))
                .setColor(0, 255, 0, 255).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        }
    }
}
// PORTED
