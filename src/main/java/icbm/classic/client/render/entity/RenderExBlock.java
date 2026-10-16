package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.mapper.ExplosiveModels;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renders a primed explosive block.
 * <p>
 * PORT: rendered like the 1.21 TntRenderer. The white flash uses the vanilla white overlay instead of the
 * 1.12 blended second pass. The model is the explosive's block model from {@link ExplosiveModels}.
 */
public class RenderExBlock extends EntityRenderer<EntityExplosive>
{
    private final BlockRenderDispatcher blockRenderer;

    public RenderExBlock(EntityRendererProvider.Context context)
    {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(EntityExplosive entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        poseStack.pushPose();
        renderBlock(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    public void renderBlock(EntityExplosive entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);

        //expansion shortly before explosion
        if ((float) entity.fuse - partialTicks + 1.0F < 10.0F)
        {
            float f = 1.0F - ((float) entity.fuse - partialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            float f1 = 1.0F + f * f * f * 0.3F;
            poseStack.scale(f1, f1, f1);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        //white flashing
        final boolean flash = entity.fuse / 5 % 2 == 0;
        renderExplosiveBlock(blockRenderer, entity.getExplosiveData(), Direction.UP, poseStack, buffer, packedLight, flash); //TODO get direction from rotation

        poseStack.popPose();
    }

    /**
     * Renders the explosive's block model at the current pose, shared with bomb carts.
     *
     * @param whiteOverlay - true to flash white like primed TNT
     */
    public static void renderExplosiveBlock(BlockRenderDispatcher blockRenderer, IExplosiveData data, Direction facing,
                                            PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean whiteOverlay)
    {
        final BlockState state = BlockReg.blockExplosive.get().defaultBlockState();
        final BakedModel model = ExplosiveModels.getBlockModel(data, facing);
        final int overlay = whiteOverlay ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : OverlayTexture.NO_OVERLAY;

        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
        {
            blockRenderer.getModelRenderer().renderModel(poseStack.last(),
                buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                state, model, 1.0F, 1.0F, 1.0F, packedLight, overlay, ModelData.EMPTY, renderType);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityExplosive entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
// PORTED
