package icbm.classic.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.client.render.entity.item.RenderItemImp;
import icbm.classic.content.missile.entity.EntityMissile;
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
public class RenderMissile extends RenderItemImp<EntityMissile> {
    public static RenderMissile INSTANCE;

    public RenderMissile(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ItemStack getRenderItem(EntityMissile entity) {
        return entity.toStack();
    }

    @Override
    protected void translate(@Nullable EntityMissile entity, IBakedModel iBakedModel, double x, double y, double z, float partialTicks) {
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
    protected void rotate(@Nullable EntityMissile entityMissile, float entityYaw, float entityPitch, float partialTicks) {
        //Rotate
        GlStateManager.rotatef(entityYaw, 0F, 1F, 0F);
        GlStateManager.rotatef(entityPitch, 1F, 0F, 0F);

        //Translate to rotation point of model TODO handle in JSON
        if (entityMissile != null) {
            GlStateManager.translated(0, -0.8, 0);
        }
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float entityYaw, float partialTicks) {
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