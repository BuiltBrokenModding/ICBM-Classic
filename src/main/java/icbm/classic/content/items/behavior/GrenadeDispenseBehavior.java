package icbm.classic.content.items.behavior;

import icbm.classic.content.entity.EntityGrenade;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class GrenadeDispenseBehavior implements DispenseItemBehavior
{
    @Override
    public ItemStack dispense(BlockSource blockSource, ItemStack itemStack)
    {
        if (itemStack.getCount() > 0)
        {
            final Level world = blockSource.level();
            if (!world.isClientSide())
            {
                world.addFreshEntity(create(world, blockSource, itemStack));
            }

            // 1.12 returned splitStack(count - 1), same as removing one
            itemStack.shrink(1);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    private Entity create(Level world, BlockSource blockSource, ItemStack itemStack)
    {
        final Direction enumFacing = blockSource.state().getValue(DispenserBlock.FACING);

        final EntityGrenade entity = new EntityGrenade(world);
        entity.setPos(blockSource.center().x, blockSource.center().y, blockSource.center().z);
        entity.setItemStack(itemStack);
        entity.setThrowableHeading(enumFacing.getStepX(), 0.10000000149011612D, enumFacing.getStepZ(), 0.5F, 1.0F);
        return entity;
    }
}
// PORTED
