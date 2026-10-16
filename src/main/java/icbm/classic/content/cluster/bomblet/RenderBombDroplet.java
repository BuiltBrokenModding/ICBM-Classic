package icbm.classic.content.cluster.bomblet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icbm.classic.ICBMConstants;
import icbm.classic.client.render.entity.RenderEntityItem2;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RenderBombDroplet extends EntityRenderer<EntityBombDroplet>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "textures/entity/fragments/fragment.png");

    private final RenderEntityItem2 renderEntityItem;

    public RenderBombDroplet(EntityRendererProvider.Context context)
    {
        super(context);
        renderEntityItem = new RenderEntityItem2(context, ItemDisplayContext.NONE);
    }

    @Override
    public void render(EntityBombDroplet entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        //Setup
        poseStack.pushPose();

        //Translate to center of entity collider
        poseStack.translate(0, 0.15, 0);

        //Rotate
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 180;
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90;
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        //Translate to rotation point of model TODO extract from model file
        poseStack.translate(0, 0.2, 0);

        //Render missile
        renderMissile(entity.toStack(), entity.level(), poseStack, buffer, packedLight);

        //Reset
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /** PORT: 1.12 rendered through a fake EntityItem, the item renderer is now called directly */
    public void renderMissile(ItemStack missileStack, Level world, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        renderEntityItem.renderStack(missileStack, world, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBombDroplet entity)
    {
        return TEXTURE;
    }
}
// PORTED
