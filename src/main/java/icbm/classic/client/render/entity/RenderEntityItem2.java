package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import icbm.classic.client.render.entity.item.RenderItemImp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * PORT: kept for callers that rendered a stack through a fake EntityItem (bomblets). Prefer
 * {@link #renderStack(ItemStack, Level, PoseStack, MultiBufferSource, int)} which needs no fake entity.
 *
 * @deprecated used {@link RenderItemImp}
 */
@Deprecated
public class RenderEntityItem2 extends EntityRenderer<ItemEntity>
{
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();
    private final ItemDisplayContext transformType;

    public RenderEntityItem2(EntityRendererProvider.Context context, ItemDisplayContext transformType)
    {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.transformType = transformType;
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        renderStack(entity.getItem(), entity.level(), poseStack, buffer, packedLight);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /** Renders the stack at the current pose like 1.12 doRender did for the fake item entity */
    public void renderStack(ItemStack itemstack, @Nullable Level level, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        int seed = itemstack.isEmpty() ? 187 : BuiltInRegistries.ITEM.getId(itemstack.getItem());
        this.random.setSeed((long) seed);

        poseStack.pushPose();
        final BakedModel ibakedmodel = this.itemRenderer.getModel(itemstack, level, null, seed);
        this.itemRenderer.render(itemstack, transformType, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ibakedmodel);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
// PORTED
