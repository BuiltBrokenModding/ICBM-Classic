package icbm.classic.content.cluster.bomblet;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.client.render.entity.item.RenderItemImp;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class RenderBombDroplet extends RenderItemImp<EntityBombDroplet>
{
    public RenderBombDroplet(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    protected ItemStack getRenderItem(EntityBombDroplet entity, int index) {
        return entity.toStack();
    }

    @Override
    protected void translate(@Nullable EntityBombDroplet entity,  ItemStack itemStack, IBakedModel iBakedModel, double x, double y, double z, float partialTicks, int index) {
        //Translate to center of entity collider
        if (entity != null) {
            GlStateManager.translated(x, y + 0.15, z); //TODO handle in JSON
        } else {
            GlStateManager.translated(x, y, z);
        }
    }

    @Override
    protected float getYaw(@Nonnull EntityBombDroplet EntityBombDroplet, float providedYaw, float partialTicks) {
        return providedYaw - 180; //TODO handle offset in JSON
    }

    @Override
    protected float getPitch(@Nonnull EntityBombDroplet EntityBombDroplet, float providedPitch, float partialTicks) {
        return providedPitch + 90; //TODO handle offset in JSON
    }

    @Override
    protected void rotate(@Nullable EntityBombDroplet EntityBombDroplet, ItemStack itemStack, float entityYaw, float entityPitch, float partialTicks, int index) {
        //Rotate
        GlStateManager.rotatef(entityYaw, 0F, 1F, 0F);
        GlStateManager.rotatef(entityPitch, 1F, 0F, 0F);

        //Translate to rotation point of model TODO handle in JSON
        if (EntityBombDroplet != null) {
            GlStateManager.translated(0, 0.2, 0);
        }
    }

    @Override
    public void doRender(EntityBombDroplet EntityBombDroplet, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(EntityBombDroplet, x, y, z, entityYaw, partialTicks);

        if (renderManager.isDebugBoundingBox()) //TODO fix so we can see motion vector
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, y, z).color(0, 255, 0, 255).endVertex();
            bufferbuilder.pos(
                    x + EntityBombDroplet.getMotion().x * 2.0D,
                    y + EntityBombDroplet.getMotion().y * 2.0D,
                    z + EntityBombDroplet.getMotion().z * 2.0D
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
