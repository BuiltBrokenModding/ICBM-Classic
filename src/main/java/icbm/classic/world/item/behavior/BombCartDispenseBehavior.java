package icbm.classic.world.item.behavior;

import icbm.classic.world.entity.BombCartEntity;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BombCartDispenseBehavior extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

    @Override
    public @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Level level = source.level();
        Vec3 pos = new Vec3(source.pos().getX(), source.pos().getY(), source.pos().getZ())
            .add(direction.getStepX() * 1.125, direction.getStepY(), direction.getStepZ() * 1.125);
        BlockPos blockPos = source.pos().offset(direction.getNormal());
        BlockState state = level.getBlockState(blockPos);

        Optional<RailShape> railShape = getRailShape(state);
        RailShape rail = railShape.orElse(RailShape.NORTH_SOUTH);

        double heightDelta;

        if (railShape.isPresent()) {
            if (rail.isAscending()) {
                heightDelta = 0.6D;
            } else {
                heightDelta = 0.1D;
            }
        } else {
            Optional<RailShape> railShapeB;
            if (state.getBlock() != Blocks.AIR || (railShapeB = getRailShape(level.getBlockState(blockPos.below()))).isEmpty()) {
                return this.DEFAULT.dispense(source, stack);
            }

            BlockState blockB = level.getBlockState(blockPos.below());
            RailShape railB = railShapeB.orElse(RailShape.NORTH_SOUTH);

            if (direction != Direction.DOWN && railB.isAscending()) {
                heightDelta = -0.4D;
            } else {
                heightDelta = -0.9D;
            }
        }

        BombCartEntity cart = new BombCartEntity(level, stack.getItemDamage());
        cart.setPos(pos.add(0, heightDelta, 0));

        if (stack.hasDisplayName()) {
            cart.setCustomNameTag(stack.getDisplayName());
        }

        world.spawnEntity(cart);
        stack.shrink(1);
        return stack;
    }

    private static Optional<RailShape> getRailShape(BlockState state) {
        if (state.hasProperty(BlockStateProperties.RAIL_SHAPE)) {
            return Optional.of(state.getValue(BlockStateProperties.RAIL_SHAPE));
        } else if (state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)) {
            return Optional.of(state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT));
        } else {
            return Optional.empty();
        }
    }
}
