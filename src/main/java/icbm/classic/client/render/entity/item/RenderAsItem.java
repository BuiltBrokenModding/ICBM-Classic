package icbm.classic.client.render.entity.item;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class RenderAsItem<E extends Entity> extends RenderItemImp<E>
{
    private final Function<E, ItemStack> itemAccessor;

    public RenderAsItem(EntityRendererProvider.Context context, Function<E, ItemStack> itemAccessor)
    {
        super(context);
        this.itemAccessor = itemAccessor;
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    protected ItemStack getRenderItem(E entity, int index)
    {
        return itemAccessor.apply(entity);
    }
}
// PORTED
