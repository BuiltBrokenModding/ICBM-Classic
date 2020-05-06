package icbm.classic.content.blast;

import icbm.classic.api.events.BlastBlockModifyEvent;
import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

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
                            float var14 = radius * (0.7F + world().rand.nextFloat() * 0.6F);
                            double var15 = location.x();
                            double var17 = location.y();
                            double var19 = location.z();

                            for (float var21 = 0.3F; var14 > 0.0F; var14 -= var21 * 0.75F)
                            {
                                BlockPos targetPosition = new BlockPos(var15, var17, var19);
                                double distanceFromCenter = location.distance(targetPosition);
                                IBlockState blockState = world().getBlockState(targetPosition);
                                Block block = blockState.getBlock();

                                if (!block.isAir(blockState, world, targetPosition))
                                {
                                    var14 -= (block.getExplosionResistance(world(), targetPosition, this.exploder, this) + 0.3F) * var21;
                                }

                                if (var14 > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        boolean canReplace = block.isReplaceable(world(), targetPosition) || block.isAir(blockState, world(), targetPosition);

                                        if (canReplace && Blocks.FIRE.canPlaceBlockAt(world(), targetPosition))
                                        {
                                            MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition, Blocks.FIRE.getDefaultState(), 3));
                                        }
                                        else if (block == Blocks.ICE)
                                        {
                                            MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition));
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

        ICBMSounds.EXPLOSION_FIRE.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D, 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
        return true;
    }
}
