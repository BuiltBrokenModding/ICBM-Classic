package icbm.classic.client.render.entity.item;

import icbm.classic.lib.data.LazyBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

/**
 * EntityItem style render
 *
 * @param <E> to render
 */
@SideOnly(Side.CLIENT)
public abstract class RenderItemImp<E extends Entity> extends Render<E>
{
    private final RenderItem itemRenderer;
    private final Random random = new Random();

    private static final Supplier<ItemStack> BACKUP_RENDER_STACK = new LazyBuilder<>(() -> new ItemStack(Blocks.FIRE));

    @Setter @Getter
    @Accessors(chain = true)
    protected boolean billboard = false;

    public RenderItemImp(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    @Nonnull
    protected abstract ItemStack getRenderItem(E entity, int index);

    protected ItemCameraTransforms.TransformType getTransformType(int index) {
        return ItemCameraTransforms.TransformType.NONE;
    }

    protected void translate(@Nullable E entity, ItemStack itemstack, IBakedModel iBakedModel, double x, double y, double z, float partialTicks, int index) {
        float hoverStart = iBakedModel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        GlStateManager.translate((float)x, (float)y + 0.25F * hoverStart, (float)z);
    }

    protected void rotate(@Nullable E entity, ItemStack itemstack, float entityYaw, float entityPitch, float partialTicks, int index) {
        // Rotate by entity yaw
        if(billboard) {
            GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F); //fish ><>
        }
        else
        {
            GlStateManager.rotate(entityYaw, 0.0F, 1.0F, 0.0F);
        }
    }

    protected void scale(@Nullable E e, ItemStack itemstack, float partialTicks, int index) {
        //GlStateManager.scale(2, 2, 2);
    }

    @Override
    public void doRender(@Nonnull E entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final float yaw = getYaw(entity, entityYaw, partialTicks); // yaw is already lerped by render manager
        final float entityPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        final float pitch = getPitch(entity, entityPitch, partialTicks);
        doRenderItem(entity, entity.world, x, y, z, yaw, pitch, partialTicks);

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
        return this.itemRenderer.getItemModelWithOverrides(stack, world, (EntityLivingBase) null);
    }

    public void renderItem(ItemStack missileStack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks)
    {
        this.renderItem(null, missileStack, world, x, y, z, entityYaw, entityPitch, partialTicks, 0);
    }

    protected void renderItem(@Nullable E entity, ItemStack itemstack, World world, double x, double y, double z, float entityYaw, float entityPitch, float partialTicks, int index) {
        if(itemstack == null || itemstack.isEmpty()) {
            itemstack = BACKUP_RENDER_STACK.get();
        }

        this.random.setSeed(Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata());
        boolean hasTexture = false;

        if (this.bindEntityTexture(entity))
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            hasTexture = true;
        }

        //Setup
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();

        IBakedModel ibakedmodel = this.getBakedModel(entity, world, itemstack);
        this.translate(entity, itemstack, ibakedmodel, x, y, z, partialTicks, index);
        this.rotate(entity, itemstack, entityYaw, entityPitch, partialTicks, index);
        this.scale(entity, itemstack, partialTicks, index);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        // Render item
        GlStateManager.pushMatrix();
        ibakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, getTransformType(index), false);
        this.itemRenderer.renderItem(itemstack, ibakedmodel);
        GlStateManager.popMatrix();

        // Reset
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);

        if (hasTexture)
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nullable E entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}