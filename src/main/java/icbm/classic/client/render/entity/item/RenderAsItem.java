package icbm.classic.client.render.entity.item;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class RenderAsItem<E extends Entity> extends RenderItemImp<E>
{
    private final Function<E, ItemStack> itemAccessor;

    public RenderAsItem(EntityRendererManager renderManagerIn, Function<E, ItemStack> itemAccessor)
    {
        super(renderManagerIn);
        this.itemAccessor = itemAccessor;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    @Override
    protected ItemStack getRenderItem(E entity, int index) {
        return itemAccessor.apply(entity);
    }
}
