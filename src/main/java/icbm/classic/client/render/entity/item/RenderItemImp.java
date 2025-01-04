package icbm.classic.client.render.entity.item;

import com.mojang.blaze3d.platform.GlStateManager;
import icbm.classic.lib.data.LazyBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.Blocks;
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
import java.util.function.Supplier;

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

    private static final LazyBuilder<ItemStack> BACKUP_RENDER_STACK = new LazyBuilder<ItemStack>(() -> new ItemStack(Items.EGG));

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

    protected abstract ItemStack getRenderItem(E entity);

    protected ItemCameraTransforms.TransformType getTransformType() {
        return ItemCameraTransforms.TransformType.NONE;
    }

    protected void translate(@Nullable E entity, net.minecraft.client.renderer.model.IBakedModel iBakedModel, double x, double y, double z, float partialTicks) {
        float hoverStart = iBakedModel.getItemCameraTransforms().getTransform(net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND).scale.getY();
        GlStateManager.translatef((float)x, (float)y + 0.25F * hoverStart, (float)z);
    }

    protected void rotate(@Nullable E entity, float entityYaw, float entityPitch, float partialTicks) {
        // Rotate by entity yaw
        if(billboard) {
            GlStateManager.rotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F); //fish ><>
        }
        else
        {
            GlStateManager.rotatef(entityYaw, 0.0F, 1.0F, 0.0F);
        }
    }

    protected void scale(@Nullable E e, float partialTicks) {
        //GlStateManager.scale(2, 2, 2);
    }

    @Override
    public void doRender(@Nonnull E entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        ItemStack itemstack = getRenderItem(entity);
        if(itemstack == null || itemstack.isEmpty()) {
            itemstack = BACKUP_RENDER_STACK.get();
        }

        final float yaw = getYaw(entity, entityYaw, partialTicks); // yaw is already lerped by render manager
        final float entityPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        final float pitch = getPitch(entity, entityPitch, partialTicks);

        this.renderItem(entity, itemstack, entity.world, x, y, z, yaw, pitch, partialTicks);

        super.doRender(entity, x, y, z, yaw, partialTicks);
    }

    protected float getYaw(@Nonnull E entity, float providedYaw, float partialTicks) {
        return providedYaw;
    }

    protected float getPitch(@Nonnull E entity, float entityPitch, float partialTicks) {
        return entity.rotationPitch;
    }

    protected net.minecraft.client.renderer.model.IBakedModel getBakedModel(@Nullable E entity, World world, ItemStack stack) {
        // TODO may need optimization, upcraft suggests caching model as doing the lookup per frame is slow.. could do per entity? or tree(item -> key -> model)
        return this.itemRenderer.getItemModelWithOverrides(stack, world, (LivingEntity) null);
    }

    public void renderItem(ItemStack missileStack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks)
    {
        this.renderItem(null, missileStack, world, x, y, z, entityYaw, entityPitch, partialTicks);
    }

    protected void renderItem(@Nullable E entity, ItemStack itemstack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks) {
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
        this.translate(entity, ibakedmodel, x, y, z, partialTicks);
        this.rotate(entity, entityYaw, entityPitch, partialTicks);
        this.scale(entity, partialTicks);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
        }

        // Render item
        GlStateManager.pushMatrix();
        ibakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, getTransformType(), false);
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