package icbm.classic.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class RenderAsItem<E extends Entity> extends Render<E>
{
    private EntityItem entityItem;
    private final RenderEntityItem renderEntityItem;
    private final Function<E, ItemStack> itemAccessor;

    public RenderAsItem(RenderManager renderManagerIn, Function<E, ItemStack> itemAccessor)
    {
        super(renderManagerIn);
        this.itemAccessor = itemAccessor;
        this.renderEntityItem = new RenderEntityItem(renderManagerIn, Minecraft.getMinecraft().getRenderItem());
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    @Override
    public void doRender(E entity, double x, double y, double z, float par8, float par9)
    {
        setupFakeItem(entity);
        renderEntityItem.doRender(entityItem, x, y, z, par8, par9);
    }

    protected void setupFakeItem(E entity) {

        //Create fake item if missing
        if(entityItem == null) {
            entityItem = new EntityItem(entity.world);
        }

        //Apply data from entity
        entityItem.setWorld(entity.world);
        entityItem.setPosition(entity.posX, entity.posY, entity.posZ);

        final ItemStack stack = this.itemAccessor.apply(entity);
        entityItem.setItem(stack != null ? stack : ItemStack.EMPTY);
    }

    @Override
    protected ResourceLocation getEntityTexture(E entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
