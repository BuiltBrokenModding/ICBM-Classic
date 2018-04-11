package icbm.classic.content.explosive.thread;

import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Used for large raycasting explosions.
 *
 * @author Calclavia
 */
public class ThreadLargeExplosion extends ThreadExplosion
{
    public IThreadCallBack callBack;

    public ThreadLargeExplosion(Blast blast, int range, float energy, Entity source, IThreadCallBack callBack)
    {
        super(blast, range, energy, source);
        this.callBack = callBack;
    }

    public ThreadLargeExplosion(Blast blast, int range, float energy, Entity source)
    {
        this(blast, range, energy, source, new BasicResistanceCallBack(blast));
    }

    @Override
    public void doRun(World world, Location center)
    {
        //How many steps to go per rotation
        final int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / this.radius));

        double x;
        double y;
        double z;

        double dx;
        double dy;
        double dz;

        for (int phi_n = 0; phi_n < 2 * steps && !kill; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps && !kill; theta_n++)
            {
                //Calculate power
                float power = this.energy - (this.energy * world.rand.nextFloat() / 2);

                //Get angles for rotation steps
                double phi = Math.PI * 2 / steps * phi_n;
                double theta = Math.PI / steps * theta_n;

                //Figure out vector to move for trace
                dx = sin(theta) * cos(phi);
                dy = cos(theta);
                dz = sin(theta) * sin(phi);

                //Reset position to current
                x = center.x();
                y = center.y();
                z = center.z();

                //Trace from start to end
                while (center.distance(x, y, z) <= this.radius && power > 0 && !kill)
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 10;

                    //Get block at position
                    final BlockPos blockPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
                    final IBlockState state = world.getBlockState(blockPos);
                    final Block block = state.getBlock();

                    //Ignore air blocks && Only break block that can be broken
                    if (!block.isAir(state, world, blockPos) && state.getBlockHardness(world, blockPos) >= 0)
                    {
                        //Consume power based on block
                        power -= this.callBack.getResistance(world, position, blockPos, source, block);

                        //If we still have power, break the block
                        if (power > 0f)
                        {
                            this.blast.addThreadResult(blockPos);
                        }
                    }

                    //Move forward
                    x += dx;
                    y += dy;
                    z += dz;
                }
            }
        }
    }
}
