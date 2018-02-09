package icbm.classic.content.explosive.thread;

import icbm.classic.content.explosive.blast.Blast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Used for small explosions.
 *
 * @author Calclavia
 */
public class ThreadSmallExplosion extends ThreadExplosion
{
    public ThreadSmallExplosion(Blast blast, int radius, Entity source)
    {
        super(blast, radius, 0, source);
    }

    @Override
    public void run()
    {
        final World world = position.world();

        if (!world.isRemote)
        {
            for (int x = 0; x < this.radius; ++x)
            {
                for (int y = 0; y < this.radius; ++y)
                {
                    for (int z = 0; z < this.radius; ++z)
                    {
                        if (x == 0 || x == this.radius - 1 || y == 0 || y == this.radius - 1 || z == 0 || z == this.radius - 1)
                        {
                            double xStep = x / (this.radius - 1.0F) * 2.0F - 1.0F;
                            double yStep = y / (this.radius - 1.0F) * 2.0F - 1.0F;
                            double zStep = z / (this.radius - 1.0F) * 2.0F - 1.0F;
                            double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);
                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;
                            float power = this.radius * (0.7F + this.position.world().rand.nextFloat() * 0.6F);
                            double var15 = position.x();
                            double var17 = position.y();
                            double var19 = position.z();

                            for (float var21 = 0.3F; power > 0.0F; power -= var21 * 0.75F)
                            {
                                BlockPos targetPosition = new BlockPos(var15, var17, var19);
                                IBlockState state = this.position.world().getBlockState(targetPosition);
                                Block blockID = state.getBlock();

                                if (blockID != Blocks.AIR)
                                {
                                    float resistance = 0;

                                    if (state.getBlockHardness(world, targetPosition) < 0)
                                    {
                                        break;
                                    }
                                    else
                                    {
                                        resistance = blockID.getExplosionResistance(world, targetPosition, source, blast);
                                    }
                                    // TODO rather than remove power divert a percentage to the
                                    // sides, and then calculate how much is absorbed by the block
                                    power -= resistance;
                                }

                                if (power > 0.0F)
                                {
                                    this.results.add(targetPosition);
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

        super.run();
    }
}
