package icbm.classic.content.explosive.blast;

import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlastFire extends Blast
{
    public BlastFire(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doExplode()
    {
        if (!this.oldWorld().isRemote)
        {
            int radius = (int) this.getRadius();

            for (int x = 0; x < radius; ++x)
            {
                for (int y = 0; y < radius; ++y)
                {
                    for (int z = 0; z < radius; ++z)
                    {
                        if (x == 0 || x == radius - 1 || y == 0 || y == radius - 1 || z == 0 || z == radius - 1)
                        {
                            double xStep = x / (radius - 1.0F) * 2.0F - 1.0F;
                            double yStep = y / (radius - 1.0F) * 2.0F - 1.0F;
                            double zStep = z / (radius - 1.0F) * 2.0F - 1.0F;
                            double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);
                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;
                            float var14 = radius * (0.7F + oldWorld().rand.nextFloat() * 0.6F);
                            double var15 = position.x();
                            double var17 = position.y();
                            double var19 = position.z();

                            for (float var21 = 0.3F; var14 > 0.0F; var14 -= var21 * 0.75F)
                            {
                                BlockPos targetPosition = new BlockPos(var15, var17, var19);
                                double distanceFromCenter = position.distance(targetPosition);
                                IBlockState blockState = oldWorld().getBlockState(targetPosition);
                                Block block = blockState.getBlock();

                                if (block != Blocks.AIR)
                                {
                                    var14 -= (block.getExplosionResistance(oldWorld(), targetPosition, this.exploder, this) + 0.3F) * var21;
                                }

                                if (var14 > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        boolean canReplace = block.isReplaceable(oldWorld(), targetPosition) || block.isAir(blockState, oldWorld(), targetPosition);

                                        if (canReplace && Blocks.FIRE.canPlaceBlockAt(oldWorld(), targetPosition))
                                        {
                                            world.setBlockState(targetPosition, Blocks.FIRE.getDefaultState(), 3);
                                        }
                                        else if (block == Blocks.ICE)
                                        {
                                            world.setBlockToAir(targetPosition);
                                        }
                                    }
                                }

                                var15 += xStep * var21;
                                var17 += yStep * var21;
                                var19 += zStep * var21;
                            }
                        }
                    }
                }
            }
        }

        ICBMSounds.EXPLOSION_FIRE.play(world, position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, 4.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 1F, true);
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
