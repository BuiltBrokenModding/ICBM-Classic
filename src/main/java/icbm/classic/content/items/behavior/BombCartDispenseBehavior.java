package icbm.classic.content.items.behavior;

import icbm.classic.content.entity.EntityBombCart;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BombCartDispenseBehavior extends BehaviorDefaultDispenseItem
{
    private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

    @Override
    public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
    {
        EnumFacing enumfacing = source.getBlockState().getValue(BlockDispenser.FACING);
        World world = source.getWorld();
        double x = source.getX() + (double) enumfacing.getFrontOffsetX() * 1.125D;
        double y = Math.floor(source.getY()) + (double) enumfacing.getFrontOffsetY();
        double z = source.getZ() + (double) enumfacing.getFrontOffsetZ() * 1.125D;
        BlockPos blockpos = source.getBlockPos().offset(enumfacing);
        IBlockState iblockstate = world.getBlockState(blockpos);
        BlockRailBase.EnumRailDirection rail =
                (iblockstate.getBlock() instanceof BlockRailBase
                        ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(world, blockpos, iblockstate, null)
                                : BlockRailBase.EnumRailDirection.NORTH_SOUTH);

        double heightDelta;

        if (BlockRailBase.isRailBlock(iblockstate))
        {
            if (rail.isAscending())
            {
                heightDelta = 0.6D;
            }
            else
            {
                heightDelta = 0.1D;
            }
        }
        else
        {
            if (iblockstate.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(blockpos.down())))
            {
                return this.behaviourDefaultDispenseItem.dispense(source, stack);
            }

            IBlockState blockB = world.getBlockState(blockpos.down());
            BlockRailBase.EnumRailDirection railB =
                    (blockB.getBlock() instanceof BlockRailBase ?
                            ((BlockRailBase) blockB.getBlock()).getRailDirection(world, blockpos.down(), blockB, null)
                            : BlockRailBase.EnumRailDirection.NORTH_SOUTH);

            if (enumfacing != EnumFacing.DOWN && railB.isAscending())
            {
                heightDelta = -0.4D;
            }
            else
            {
                heightDelta = -0.9D;
            }
        }

        EntityBombCart cart = new EntityBombCart(world, x, y + heightDelta, z, stack);

        if (stack.hasDisplayName())
        {
            cart.setCustomNameTag(stack.getDisplayName());
        }

        world.spawnEntity(cart);
        stack.shrink(1);
        return stack;
    }
}
