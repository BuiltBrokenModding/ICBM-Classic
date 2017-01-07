package icbm.classic.content.explosive.thread;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/** Used for large raycasting explosions.
 *
 * @author Calclavia */
public class ThreadLargeExplosion extends ThreadExplosion
{
    public static interface IThreadCallBack
    {
        public float getResistance(World world, Pos position, Pos targetPosition, Entity source, Block block);
    }

    public IThreadCallBack callBack;

    public ThreadLargeExplosion(VectorWorld position, int range, float energy, Entity source, IThreadCallBack callBack)
    {
        super(position, range, energy, source);
        this.callBack = callBack;
    }

    public ThreadLargeExplosion(VectorWorld position, int range, float energy, Entity source)
    {
        this(position, range, energy, source, new IThreadCallBack()
        {

            @Override
            public float getResistance(World world, Pos pos, Pos targetPosition, Entity source, Block block)
            {
                float resistance = 0;

                if (block instanceof BlockFluid || block instanceof IFluidBlock)
                {
                    resistance = 0.25f;
                }
                else
                {
                    resistance = block.getExplosionResistance(source, world, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ(), pos.intX(), pos.intY(), pos.intZ());
                }

                return resistance;
            }

        });
    }

    @Override
    public void run()
    {
        int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / this.radius));

        for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps; theta_n++)
            {
                double phi = Math.PI * 2 / steps * phi_n;
                double theta = Math.PI / steps * theta_n;

                Pos delta = new Pos(sin(theta) * cos(phi), cos(theta), sin(theta) * sin(phi));
                float power = this.energy - (this.energy * this.position.world().rand.nextFloat() / 2);

                Pos t = position.clone();

                for (float d = 0.3F; power > 0f; power -= d * 0.75F * 10)
                {
                    if (t.distance(position) > this.radius)
                        break;

                    Block block = Block.blocksList[this.position.world().getBlockId(t.intX(), t.intY(), t.intZ())];

                    if (block != null)
                    {
                        if (block.getBlockHardness(this.position.world(), t.intX(), t.intY(), t.intZ()) >= 0)
                        {
                            power -= this.callBack.getResistance(this.position.world(), position, t, source, block);

                            if (power > 0f)
                            {
                                this.results.add(t.clone());
                            }

                        }
                    }
                    t.translate(delta);
                }
            }
        }

        super.run();
    }
}
