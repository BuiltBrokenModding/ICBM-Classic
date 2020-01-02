package icbm.classic.content.items.behavior;

import icbm.classic.content.entity.EntityGrenade;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class GrenadeDispenseBehavior implements IBehaviorDispenseItem
{
    @Override
    public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
    {
        if (itemStack.getCount() > 0)
        {
            final World world = blockSource.getWorld();
            if (!world.isRemote)
            {
                world.spawnEntity(create(world, blockSource, itemStack));
            }

            return itemStack.splitStack(itemStack.getCount() - 1);
        }
        return ItemStack.EMPTY;
    }

    private Entity create(World world, IBlockSource blockSource, ItemStack itemStack)
    {
        final EnumFacing enumFacing = blockSource.getBlockState().getValue(BlockDispenser.FACING);

        final EntityGrenade entity = new EntityGrenade(world);
        entity.setPosition(blockSource.getX(), blockSource.getY(), blockSource.getZ());
        entity.setItemStack(itemStack);
        entity.setThrowableHeading(enumFacing.getXOffset(), 0.10000000149011612D, enumFacing.getZOffset(), 0.5F, 1.0F);
        return entity;
    }
}
