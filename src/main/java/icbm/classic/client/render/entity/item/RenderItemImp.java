package icbm.classic.client.render.entity.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icbm.classic.lib.data.LazyBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

/**
 * ItemEntity style render
 * <p>
 * PORT: GL state calls replaced with the PoseStack. The pose already sits at the entity position, so the
 * 1.12 x/y/z offsets are gone from the hooks. A fixed standalone model can be set with {@link #setModelOverride}
 * (1.12 used a fake item meta for the 3D balloon).
 *
 * @param <E> to render
 */
public abstract class RenderItemImp<E extends Entity> extends EntityRenderer<E>
{
    protected final ItemRenderer itemRenderer;
    private final Random random = new Random();

    private static final Supplier<ItemStack> BACKUP_RENDER_STACK = new LazyBuilder<>(() -> new ItemStack(Blocks.FIRE));

    @Setter @Getter
    @Accessors(chain = true)
    protected boolean billboard = false;

    /** Standalone model to render instead of the stack's item model, null to use the item model */
    @Setter @Getter
    @Accessors(chain = true)
    protected ModelResourceLocation modelOverride = null;

    public RenderItemImp(EntityRendererProvider.Context context)
    {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Nonnull
    protected abstract ItemStack getRenderItem(E entity, int index);

    protected ItemDisplayContext getTransformType(int index)
    {
        return ItemDisplayContext.NONE;
    }

    protected void translate(@Nullable E entity, ItemStack itemstack, BakedModel iBakedModel, PoseStack poseStack, float partialTicks, int index)
    {
        final float hoverStart = iBakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        poseStack.translate(0, 0.25F * hoverStart, 0);
    }

    protected void rotate(@Nullable E entity, ItemStack itemstack, PoseStack poseStack, float entityYaw, float entityPitch, float partialTicks, int index)
    {
        // Rotate by entity yaw
        if (billboard)
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.camera.getYRot())); //fish ><>
        }
        else
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        }
    }

    protected void scale(@Nullable E e, ItemStack itemstack, PoseStack poseStack, float partialTicks, int index)
    {
        //poseStack.scale(2, 2, 2);
    }

    @Override
    public void render(@Nonnull E entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        final float yaw = getYaw(entity, entityYaw, partialTicks); // yaw is already lerped by render manager
        final float entityPitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;
        final float pitch = getPitch(entity, entityPitch, partialTicks);
        doRenderItem(entity, entity.level(), poseStack, buffer, packedLight, yaw, pitch, partialTicks);

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected void doRenderItem(@Nonnull E entity, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float yaw, float pitch, float partialTicks)
    {
        this.renderItem(entity, getRenderItem(entity, 0), world, poseStack, buffer, packedLight, yaw, pitch, partialTicks, 0);
    }

    protected float getYaw(@Nonnull E entity, float providedYaw, float partialTicks)
    {
        return providedYaw;
    }

    protected float getPitch(@Nonnull E entity, float entityPitch, float partialTicks)
    {
        return entity.getXRot();
    }

    protected BakedModel getBakedModel(@Nullable E entity, Level world, ItemStack stack)
    {
        if (modelOverride != null)
        {
            return Minecraft.getInstance().getModelManager().getModel(modelOverride);
        }
        // TODO may need optimization, upcraft suggests caching model as doing the lookup per frame is slow.. could do per entity? or tree(item -> key -> model)
        return this.itemRenderer.getModel(stack, world, null, 0);
    }

    public void renderItem(ItemStack missileStack, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float entityYaw, float entityPitch, float partialTicks)
    {
        this.renderItem(null, missileStack, world, poseStack, buffer, packedLight, entityYaw, entityPitch, partialTicks, 0);
    }

    protected void renderItem(@Nullable E entity, ItemStack itemstack, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                              float entityYaw, float entityPitch, float partialTicks, int index)
    {
        if (itemstack == null || itemstack.isEmpty())
        {
            itemstack = BACKUP_RENDER_STACK.get();
        }

        this.random.setSeed(BuiltInRegistries.ITEM.getId(itemstack.getItem()));

        poseStack.pushPose();

        final BakedModel ibakedmodel = this.getBakedModel(entity, world, itemstack);
        this.translate(entity, itemstack, ibakedmodel, poseStack, partialTicks, index);
        this.rotate(entity, itemstack, poseStack, entityYaw, entityPitch, partialTicks, index);
        this.scale(entity, itemstack, poseStack, partialTicks, index);

        // Render item, applies the display transform of the context and centers the model like 1.12 renderItem
        this.itemRenderer.render(itemstack, getTransformType(index), false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ibakedmodel);

        poseStack.popPose();
    }

    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nullable E entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
// PORTED
