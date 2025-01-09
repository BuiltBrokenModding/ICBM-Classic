package icbm.classic.content.blast;

import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BlastFire extends Blast
{
    public BlastFire()
    {
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.getWorld().isRemote)
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

                            float energy = radius * (0.7F + getWorld().rand.nextFloat() * 0.6F);
                            double posX = getPosition().x;
                            double posY = getPosition().y;
                            double posZ = getPosition().z;

                            for (float stepAmount = 0.3F; energy > 0.0F; energy -= stepAmount * 0.75F)
                            {
                                BlockPos targetPosition = new BlockPos(posX, posY, posZ);

                                final double delta_x = getPosition().x - targetPosition.getX();
                                final double delta_y = getPosition().y - targetPosition.getY();
                                final double delta_z = getPosition().z - targetPosition.getZ();

                                final double distanceFromCenter = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);

                                BlockState blockState = getWorld().getBlockState(targetPosition);
                                Block block = blockState.getBlock();

                                if (!block.isAir(blockState, world, targetPosition))
                                {
                                    energy -= (block.getExplosionResistance(blockState, getWorld(), targetPosition, this.exploder, this) + 0.3F) * stepAmount;
                                }

                                if (energy > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        boolean canReplace = blockState.isReplaceable(new DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) || block.isAir(blockState, getWorld(), targetPosition);

                                        if (canReplace && Blocks.FIRE.isValidPosition(blockState, getWorld(), targetPosition))
                                        {
                                            world.setBlockState(targetPosition, Blocks.FIRE.getDefaultState(), 3);
                                        }
                                        else if (block == Blocks.ICE)
                                        {
                                            world.removeBlock(targetPosition, false);
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

        ICBMSounds.EXPLOSION_FIRE.play(world,
            getPosition().x + 0.5D, getPosition().y + 0.5D, getPosition().z + 0.5D,
            4.0F, (1.0F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.2F) * 1F, true);
        return true;
    }
}
