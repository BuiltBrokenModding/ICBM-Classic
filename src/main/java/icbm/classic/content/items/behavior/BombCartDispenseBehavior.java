package icbm.classic.content.items.behavior;

import icbm.classic.content.entity.EntityBombCart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class BombCartDispenseBehavior extends DefaultDispenseItemBehavior
{
    private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

    private static RailShape getRailShape(Level world, BlockPos pos, BlockState state)
    {
        return state.getBlock() instanceof BaseRailBlock rail
            ? rail.getRailDirection(state, world, pos, null)
            : RailShape.NORTH_SOUTH;
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack)
    {
        final Direction enumfacing = source.state().getValue(DispenserBlock.FACING);
        final Level world = source.level();
        final double x = source.center().x + (double) enumfacing.getStepX() * 1.125D;
        final double y = Math.floor(source.center().y) + (double) enumfacing.getStepY();
        final double z = source.center().z + (double) enumfacing.getStepZ() * 1.125D;
        final BlockPos blockpos = source.pos().relative(enumfacing);
        final BlockState iblockstate = world.getBlockState(blockpos);
        final RailShape rail = getRailShape(world, blockpos, iblockstate);

        double heightDelta;

        if (BaseRailBlock.isRail(iblockstate))
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
            if (!iblockstate.isAir() || !BaseRailBlock.isRail(world.getBlockState(blockpos.below())))
            {
                return this.behaviourDefaultDispenseItem.dispense(source, stack);
            }

            final BlockState blockB = world.getBlockState(blockpos.below());
            final RailShape railB = getRailShape(world, blockpos.below(), blockB);

            if (enumfacing != Direction.DOWN && railB.isAscending())
            {
                heightDelta = -0.4D;
            }
            else
            {
                heightDelta = -0.9D;
            }
        }

        final EntityBombCart cart = new EntityBombCart(world, x, y + heightDelta, z, stack);

        if (stack.has(DataComponents.CUSTOM_NAME))
        {
            cart.setCustomName(stack.getHoverName());
        }

        world.addFreshEntity(cart);
        stack.shrink(1);
        return stack;
    }
}
// PORTED
