package icbm.classic.content.explosive.thread;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.explosive.blast.Blast;
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
    public void run()
    {
        final World world = position.world;
        int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / this.radius));

        for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps; theta_n++)
            {
                double phi = Math.PI * 2 / steps * phi_n;
                double theta = Math.PI / steps * theta_n;

                Pos delta = new Pos(sin(theta) * cos(phi), cos(theta), sin(theta) * sin(phi));
                float power = this.energy - (this.energy * world.rand.nextFloat() / 2);

                Pos pos = new Pos(position.xi(), position.yi(), position.zi());

                for (float d = 0.3F; power > 0f; power -= d * 0.75F * 10)
                {
                    if (position.distance(pos) > this.radius)
                    {
                        break;
                    }

                    final BlockPos blockPos = new BlockPos(pos.xi(), pos.yi(), pos.zi());
                    final IBlockState state = world.getBlockState(blockPos);
                    final Block block = state.getBlock();

                    if (!block.isAir(state, world, blockPos))
                    {
                        if (state.getBlockHardness(world, blockPos) >= 0)
                        {
                            power -= this.callBack.getResistance(world, position, blockPos, source, block);

                            if (power > 0f)
                            {
                                this.results.add(blockPos);
                            }
                        }
                    }
                    pos = pos.add(delta);
                }
            }
        }

        super.run();
    }
}
