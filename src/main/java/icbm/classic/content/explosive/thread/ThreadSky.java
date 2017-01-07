package icbm.classic.content.explosive.thread;

import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Used for searching block spawn. Returns a block above this found block coordinate.
 *
 * @author Calclavia
 */
public class ThreadSky extends ThreadExplosion
{
    public static interface IThreadCallBack
    {
        public float getResistance(World world, Pos position, Pos targetPosition, Entity source, Block block);
    }

    public IThreadCallBack callBack;

    public ThreadSky(Location position, int banJing, float nengLiang, Entity source, IThreadCallBack callBack)
    {
        super(position, banJing, nengLiang, source);
        this.callBack = callBack;
    }

    public ThreadSky(Location position, int banJing, float nengLiang, Entity source)
    {
        this(position, banJing, nengLiang, source, new IThreadCallBack()
        {

            @Override
            public float getResistance(World world, Pos explosionPosition, Pos targetPosition, Entity source, Block block)
            {
                float resistance = 0;

                if (block instanceof BlockLiquid || block instanceof IFluidBlock)
                {
                    resistance = 0.25f;
                }
                else
                {
                    resistance = block.getExplosionResistance(source, world, targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), explosionPosition.xi(), explosionPosition.yi(), explosionPosition.zi());
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

                Location targetPosition = this.position;

                for (float var21 = 0.3F; power > 0f; power -= var21 * 0.75F * 10)
                {
                    if (targetPosition.distance(position) > this.radius)
                    {
                        break;
                    }

                    Block blockID = this.position.world().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                    if (blockID != Blocks.air)
                    {
                        if (blockID == Blocks.bedrock)
                        {
                            break;
                        }

                        float resistance = this.callBack.getResistance(this.position.world(), position.toPos(), targetPosition.toPos(), source, blockID);

                        power -= resistance;

                        if (power > 0f)
                        {
                            this.results.add(targetPosition.add(new Pos(0, 1, 0)).toPos());
                        }
                    }

                    targetPosition = targetPosition.add(delta);
                }
            }
        }
        super.run();
    }
}
