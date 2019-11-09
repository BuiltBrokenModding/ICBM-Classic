package icbm.classic.content.blast.thread;

import com.builtbroken.jlib.lang.StringHelpers;
import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.Blast;
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
@Deprecated
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
        long time = System.nanoTime();

        //How many steps to go per rotation
        final int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / this.radius));

        double x;
        double y;
        double z;

        double dx;
        double dy;
        double dz;

        double power;

        double yaw;
        double pitch;

        for (int phi_n = 0; phi_n < 2 * steps && !kill; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps && !kill; theta_n++)
            {
                //Calculate power
                power = this.energy - (this.energy * world.rand.nextFloat() / 2);

                //Get angles for rotation steps
                yaw = Math.PI * 2 / steps * phi_n;
                pitch = Math.PI / steps * theta_n;

                //Figure out vector to move for trace (cut in half to improve trace skipping blocks)
                dx = sin(pitch) * cos(yaw) * 0.5;
                dy = cos(pitch) * 0.5;
                dz = sin(pitch) * sin(yaw) * 0.5;

                //Reset position to current
                x = center.x();
                y = center.y();
                z = center.z();

                BlockPos prevPos = null;

                //Trace from start to end
                while (center.distance(x, y, z) <= this.radius && power > 0 && !kill)
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 5; //TODO why the magic numbers?

                    //Convert double position to int position as block pos
                    final BlockPos blockPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));

                    //Only do action one time per block (not a perfect solution, but solves double hit on the same block in the same line)
                    if (prevPos != blockPos)
                    {
                        if(!position.world().isBlockLoaded(blockPos)) //TODO: find better fix for non main thread loading
                            continue;

                        //Get block state and block from position
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
                    }

                    //Note previous block
                    prevPos = blockPos;

                    //Move forward
                    x += dx;
                    y += dy;
                    z += dz;
                }
            }
        }

        if (ConfigDebug.DEBUG_THREADS)
        {
            time = System.nanoTime() - time;
            String timeString = StringHelpers.formatNanoTime(time);
            String msg = "ThreadLargeExplosion#run() -> Completed calculation in [%s] \nBlast: %s\nCompleted: %s\nRadius: %s\nEnergy: %s";
            ICBMClassic.logger().info(String.format(msg, timeString, blast, !kill, radius, energy));
        }
    }
}
