package icbm.classic.content.blast;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.potion.CustomPotionEffect;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import icbm.classic.content.blast.helpers.BlastBlockHelpers;

import java.util.Iterator;
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
            final double delta_x = location.xi() - targetPosition.getX();
            final double delta_y = location.yi() - targetPosition.getY();
            final double delta_z = location.zi() - targetPosition.getZ();

            final double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);
            final double distanceScale = 1 - (distance / radius);

            BlockState blockState = world.getBlockState(targetPosition);

            //Closer to center the better the chance of spawning blocks
            if (distance <= radiusDecay || Math.random() < distanceScale)
            {
                //Turn fluids and liquid like blocks to air
                if (BlastBlockHelpers.isWaterMaterial(blockState))
                {
                    this.world().setBlock(targetPosition, Blocks.ICE.defaultBlockState(), 3);
                }

                else if (blockState.getBlock() == Blocks.FIRE)
                {
                    world.removeBlock(targetPosition, false);
                }
                // PORT: 1.12 LAVA was the still block and FLOWING_LAVA the flowing one, 1.21 has one block, level 0 is a source
                else if (blockState.getBlock() == Blocks.LAVA && blockState.getValue(LiquidBlock.LEVEL) == 0)
                {
                    world.setBlock(targetPosition, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
                else if (blockState.getBlock() == Blocks.LAVA)
                {
                    int level = Math.min(8, Math.max(1, blockState.getValue(LiquidBlock.LEVEL) / 2));
                    world.setBlock(targetPosition, Blocks.SNOW.defaultBlockState()
                            .setValue(SnowLayerBlock.LAYERS, level), 3);
                }
                else if (blockState.getBlock() == Blocks.MAGMA_BLOCK)
                {
                    world.setBlock(targetPosition, Blocks.STONE.defaultBlockState(), 3);
                }
                else if (blockState.getBlock() == Blocks.NETHERRACK)
                {
                    world.setBlock(targetPosition, Blocks.DIRT.defaultBlockState(), 3);
                }
                else if (blockState.getBlock() == Blocks.SOUL_SAND)
                {
                    if (world.random.nextBoolean())
                    {
                        world.setBlock(targetPosition, Blocks.SAND.defaultBlockState(), 3);
                    }
                    else
                    {
                        world.setBlock(targetPosition, Blocks.GRAVEL.defaultBlockState(), 3);
                    }
                }

                //Ground replacement
                else if (BlastBlockHelpers.isGroundMaterial(blockState))
                {
                    if (world.random.nextBoolean())
                    {
                        this.world().setBlock(targetPosition, Blocks.ICE.defaultBlockState(), 3);
                    }
                    else
                    {
                        this.world().setBlock(targetPosition, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                    }
                }

                //Randomly place fire TODO move to outside mutate so we always place snow while charging up
                if (Math.random() < distanceScale)
                {
                    tryPlaceSnow(world, targetPosition.above(), false);
                }
            }
        }
    }

    private static void tryPlaceSnow(Level world, BlockPos pos, boolean random)
    {
        if (!random || world.random.nextBoolean())
        {
            //Place fire
            final BlockState blockState = world.getBlockState(pos);
            final BlockState blockStateUnder = world.getBlockState(pos.below());
            if (blockState.canBeReplaced()
                    && Blocks.SNOW.defaultBlockState().canSurvive(world, pos)
                    && blockStateUnder.isFaceSturdy(world, pos.below(), Direction.UP))
            {
                world.setBlock(pos, Blocks.SNOW.defaultBlockState()
                        .setValue(SnowLayerBlock.LAYERS, 1 + world.random.nextInt(7)), 3);

            }
        }
    }

    @Override
    protected void onBlastCompleted()
    {
        super.onBlastCompleted();

        //Freeze all nearby entities.
        final List<Mob> livingEntities = world().getEntitiesOfClass(Mob.class, new AABB(location.x() - getBlastRadius(), location.y() - getBlastRadius(), location.z() - getBlastRadius(), location.x() + getBlastRadius(), location.y() + getBlastRadius(), location.z() + getBlastRadius()));

        if (livingEntities != null && !livingEntities.isEmpty())
        {
            for (Mob entity : livingEntities) {
                if (entity != null && entity.isAlive()) {
                    //entity.addEffect(new CustomPotionEffect(PoisonFrostBite.INSTANCE, 60 * 20, 1, null));
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 2));
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120 * 20, 2));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120 * 20, 4));
                }
            }
        }

        //Change to time
        if (ConfigBlast.ALLOW_DAY_NIGHT && world().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) && world() instanceof ServerLevel serverLevel)
        {
            serverLevel.setDayTime(1200);
        }
    }
}
// PORTED
