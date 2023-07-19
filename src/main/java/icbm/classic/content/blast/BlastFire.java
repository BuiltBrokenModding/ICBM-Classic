package icbm.classic.content.blast;

import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class BlastFire extends Blast
{
    public BlastFire()
    {
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isRemote)
        {
            int radius = (int) this.getBlastRadius();

            for (int x = 0; x < radius; ++x) //TODO replace with edge-raytracer
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

                            float energy = radius * (0.7F + world().rand.nextFloat() * 0.6F);
                            double posX = location.x();
                            double posY = location.y();
                            double posZ = location.z();

                            for (float stepAmount = 0.3F; energy > 0.0F; energy -= stepAmount * 0.75F)
                            {
                                BlockPos targetPosition = new BlockPos(posX, posY, posZ);
                                double distanceFromCenter = location.distance(targetPosition);
                                IBlockState blockState = world().getBlockState(targetPosition);
                                Block block = blockState.getBlock();

                                if (!block.isAir(blockState, world, targetPosition))
                                {
                                    energy -= (block.getExplosionResistance(world(), targetPosition, this.exploder, this) + 0.3F) * stepAmount;
                                }

                                if (energy > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        boolean canReplace = block.isReplaceable(world(), targetPosition) || block.isAir(blockState, world(), targetPosition);

                                        if (canReplace && Blocks.FIRE.canPlaceBlockAt(world(), targetPosition))
                                        {
                                            world.setBlockState(targetPosition, Blocks.FIRE.getDefaultState(), 3);
                                        }
                                        else if (block == Blocks.ICE)
                                        {
                                            world.setBlockToAir(targetPosition);
                                        }
                                    }
                                }

                                posX += xStep * stepAmount;
                                posY += yStep * stepAmount;
                                posZ += zStep * stepAmount;
                            }
                        }
                    }
                }
            }
        }

        ICBMSounds.EXPLOSION_FIRE.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D, 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
        return true;
    }
}
