package icbm.classic.content.items.behavior;

import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class GrenadeDispenseBehavior implements IBehaviorDispenseItem
{
    @Override
    public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
    {
        World world = blockSource.getWorld();

        if (!world.isRemote)
        {
            EnumFacing enumFacing = blockSource.getBlockState().getValue(BlockDispenser.FACING);

            EntityGrenade entity = new EntityGrenade(world, new Pos(blockSource.getBlockPos()), itemStack.getItemDamage());
            entity.setThrowableHeading(enumFacing.getXOffset(), 0.10000000149011612D, enumFacing.getZOffset(), 0.5F, 1.0F);
            world.spawnEntity(entity);
        }

        itemStack.shrink(1);
        return itemStack;
    }
}
