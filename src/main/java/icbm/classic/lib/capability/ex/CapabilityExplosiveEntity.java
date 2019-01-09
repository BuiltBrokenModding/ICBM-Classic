package icbm.classic.lib.capability.ex;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveEntity extends CapabilityExplosive
{

    public final ItemStack stack;
    public final Entity entity;

    public CapabilityExplosiveEntity(Entity entity, ItemStack stack)
    {
        super(stack.getItemDamage());
        this.stack = stack;
        this.entity = entity;
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        return stack;
    }

    @Override
    public void onDefuse()
    {
        entity.world.spawnEntity(new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, toStack().copy()));
    }
}
