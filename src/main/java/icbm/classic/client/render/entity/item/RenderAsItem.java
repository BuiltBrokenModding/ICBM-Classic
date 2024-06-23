package icbm.classic.client.render.entity.item;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class RenderAsItem<E extends Entity> extends RenderItemImp<E>
{
    private final Function<E, ItemStack> itemAccessor;

    public RenderAsItem(RenderManager renderManagerIn, Function<E, ItemStack> itemAccessor)
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
