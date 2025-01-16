package icbm.classic.client.render.entity.item;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.lib.data.LazyBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * EntityItem style render
 *
 * @param <E> to render
 */
@OnlyIn(Dist.CLIENT)
public abstract class RenderItemImp<E extends Entity> extends EntityRenderer<E>
{
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    protected static final LazyBuilder<ItemStack> BACKUP_RENDER_STACK = new LazyBuilder<ItemStack>(() -> new ItemStack(Items.EGG));

    @Setter @Getter
    @Accessors(chain = true)
    protected boolean billboard = false;

    public RenderItemImp(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    protected abstract ItemStack getRenderItem(E entity, int index);

    protected ItemCameraTransforms.TransformType getTransformType(int index) {
        return ItemCameraTransforms.TransformType.NONE;
    }

    protected void translate(@Nullable E entity, ItemStack itemStack, IBakedModel iBakedModel, double x, double y, double z, float partialTicks, int index) {
        float hoverStart = iBakedModel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
        GlStateManager.translatef((float)x, (float)y + 0.25F * hoverStart, (float)z);
    }

    protected void rotate(@Nullable E entity, ItemStack itemStack, float entityYaw, float entityPitch, float partialTicks, int index) {
        // Rotate by entity yaw
        if(billboard) {
            GlStateManager.rotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F); //fish ><>
        }
        else
        {
            GlStateManager.rotatef(entityYaw, 0.0F, 1.0F, 0.0F);
        }
    }

    protected void scale(@Nullable E e, ItemStack itemStack, float partialTicks, int index) {
        //GlStateManager.scale(2, 2, 2);
    }

    @Override
    public void doRender(@Nonnull E entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final float yaw = getYaw(entity, entityYaw, partialTicks); // yaw is already lerped by render manager
        final float entityPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        final float pitch = getPitch(entity, entityPitch, partialTicks);
        this.doRenderItem(entity, entity.world, x, y, z, yaw, pitch, partialTicks);

        super.doRender(entity, x, y, z, yaw, partialTicks);
    }

    protected void doRenderItem(@Nonnull E entity, World world, double x, double y, double z, float yaw, float pitch, float partialTicks) {
        this.renderItem(entity, getRenderItem(entity, 0), entity.world, x, y, z, yaw, pitch, partialTicks, 0);
    }


    protected float getYaw(@Nonnull E entity, float providedYaw, float partialTicks) {
        return providedYaw;
    }

    protected float getPitch(@Nonnull E entity, float entityPitch, float partialTicks) {
        return entity.rotationPitch;
    }

    protected IBakedModel getBakedModel(@Nullable E entity, World world, ItemStack stack) {
        // TODO may need optimization, upcraft suggests caching model as doing the lookup per frame is slow.. could do per entity? or tree(item -> key -> model)
        return this.itemRenderer.getItemModelWithOverrides(stack, world, (LivingEntity) null);
    }

    public void renderItem(ItemStack missileStack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks)
    {
        this.renderItem(null, missileStack, world, x, y, z, entityYaw, entityPitch, partialTicks, 0);
    }

    protected void renderItem(@Nullable E entity, ItemStack itemstack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks, int index) {
        if(itemstack == null || itemstack.isEmpty()) {
            itemstack = BACKUP_RENDER_STACK.get();
        }

        this.random.setSeed(Item.getIdFromItem(itemstack.getItem()) + itemstack.getDamage());
        boolean hasTexture = false;

        if (this.bindEntityTexture(entity))
        {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            hasTexture = true;
        }

        //Setup
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();

        IBakedModel ibakedmodel = this.getBakedModel(entity, world, itemstack);
        this.translate(entity, itemstack, ibakedmodel, x, y, z, partialTicks, index);
        this.rotate(entity, itemstack, entityYaw, entityPitch, partialTicks, index);
        this.scale(entity, itemstack, partialTicks, index);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
        }

        // Render item
        GlStateManager.pushMatrix();
        ibakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, getTransformType(index), false);
        this.itemRenderer.renderItem(itemstack, ibakedmodel);
        GlStateManager.popMatrix();

        // Reset
        if (this.renderOutlines)
        {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);

        if (hasTexture)
        {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nullable E entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}