package icbm.classic.content.blast;

import icbm.classic.config.blast.ConfigBlast;
import net.minecraft.block.*;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class BlastEndothermic extends BlastBeam
{
    public BlastEndothermic()
    {
        this.red = 0f;
        this.green = 0.3f;
        this.blue = 0.7f;
    }

    @Override
    protected void mutateBlocks(List<BlockPos> edits)
    {
        final double radius = this.getBlastRadius();
        final double radiusDecay = Math.max(1, radius * 0.3); //TODO config
        for (BlockPos targetPosition : edits)
        {
            final double delta_x = xi() - targetPosition.getX();
            final double delta_y = yi() - targetPosition.getY();
            final double delta_z = zi() - targetPosition.getZ();

            final double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);
            final double distanceScale = 1 - (distance / radius);

            BlockState blockState = world.getBlockState(targetPosition);

            //Closer to center the better the chance of spawning blocks
            if (distance <= radiusDecay || Math.random() < distanceScale)
            {
                //Turn fluids and liquid like blocks to air
                if (blockState.getMaterial() == Material.WATER)
                {
                    this.world().setBlockState(targetPosition, net.minecraft.block.Blocks.ICE.getDefaultState(), 3);
                }

                else if (blockState.getBlock() == net.minecraft.block.Blocks.FIRE)
                {
                    world.removeBlock(targetPosition, false);
                }
                else if (blockState.getBlock() == net.minecraft.block.Blocks.LAVA)
                {
                    world.setBlockState(targetPosition, Blocks.OBSIDIAN.getDefaultState());
                }
                else if (blockState.getBlock() == Blocks.MAGMA_BLOCK)
                {
                    world.setBlockState(targetPosition, net.minecraft.block.Blocks.STONE.getDefaultState(), 3);
                }
                else if (blockState.getBlock() == net.minecraft.block.Blocks.NETHERRACK)
                {
                    world.setBlockState(targetPosition, net.minecraft.block.Blocks.DIRT.getDefaultState(), 3);
                }
                else if (blockState.getBlock() == net.minecraft.block.Blocks.SOUL_SAND)
                {
                    if (world.rand.nextBoolean())
                    {
                        world.setBlockState(targetPosition, net.minecraft.block.Blocks.SAND.getDefaultState(), 3);
                    }
                    else
                    {
                        world.setBlockState(targetPosition, net.minecraft.block.Blocks.GRAVEL.getDefaultState(), 3);
                    }
                }

                //Ground replacement
                else if (blockState.getMaterial() == Material.EARTH)
                {
                    if (world.rand.nextBoolean())
                    {
                        this.world().setBlockState(targetPosition, net.minecraft.block.Blocks.ICE.getDefaultState(), 3);
                    }
                    else
                    {
                        this.world().setBlockState(targetPosition, net.minecraft.block.Blocks.SNOW.getDefaultState(), 3);
                    }
                }

                //Randomly place fire TODO move to outside mutate so we always place snow while charging up
                if (Math.random() < distanceScale)
                {
                    tryPlaceSnow(world, targetPosition.up(), false);
                }
            }
        }
    }

    private static void tryPlaceSnow(World world, BlockPos pos, boolean random)
    {

        if (!random || world.rand.nextBoolean())
        {
            //Place fire
            final BlockState blockState = world.getBlockState(pos);

            final BlockState snowState = Blocks.SNOW.getDefaultState()
                .with(SnowBlock.LAYERS, 1 + world.rand.nextInt(7));

            if (blockState.isReplaceable(new DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP))
                && Blocks.SNOW.isValidPosition(snowState, world, pos))
            {
                world.setBlockState(pos, snowState, 3);

            }
        }
    }

    @Override
    protected void onBlastCompleted()
    {
        super.onBlastCompleted();

        //Freeze all nearby entities.
        final List<MobEntity> livingEntities = world().getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(
            x() - getBlastRadius(), y() - getBlastRadius(), z() - getBlastRadius(),
            x() + getBlastRadius(), y() + getBlastRadius(), z() + getBlastRadius()));

        if (livingEntities != null && !livingEntities.isEmpty())
        {
            for (MobEntity entity : livingEntities) {
                if (entity != null && entity.isAlive()) {
                    //entity.addPotionEffect(new CustomPotionEffect(PoisonFrostBite.INSTANCE, 60 * 20, 1, null));
                    entity.addPotionEffect(new EffectInstance(Effects.POISON, 10 * 20, 2));
                    entity.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 120 * 20, 2));
                    entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 120 * 20, 4));
                }
            }
        }

        //Change to time
        if (ConfigBlast.ALLOW_DAY_NIGHT && world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
        {
            this.world().setDayTime(1200);
        }
    }
}
