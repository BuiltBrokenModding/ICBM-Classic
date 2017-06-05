package icbm.classic.content.explosive.thread;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Used for large raycasting explosions.
 *
 * @author Calclavia
 */
public class ThreadLargeExplosion extends ThreadExplosion
{
    public static interface IThreadCallBack
    {
        public float getResistance(World world, IPos3D position, IPos3D targetPosition, Entity source, Block block);
    }

    public IThreadCallBack callBack;

    public ThreadLargeExplosion(IWorldPosition position, int range, float energy, Entity source, IThreadCallBack callBack)
    {
        super(position, range, energy, source);
        this.callBack = callBack;
    }

    public ThreadLargeExplosion(IWorldPosition position, int range, float energy, Entity source)
    {
        this(position, range, energy, source, new IThreadCallBack()
        {

            @Override
            public float getResistance(World world, IPos3D pos, IPos3D targetPosition, Entity source, Block block)
            {
                float resistance = 0;

                if (block instanceof BlockLiquid || block instanceof IFluidBlock)
                {
                    resistance = 0.25f;
                }
                else
                {
                    resistance = block.getExplosionResistance(source, world, (int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z(), pos.x(), pos.y(), pos.z());
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

                Location t = position;

                for (float d = 0.3F; power > 0f; power -= d * 0.75F * 10)
                {
                    if (t.distance(position) > this.radius)
                    {
                        break;
                    }

                    Block block = t.getBlock();

                    if (block != null)
                    {
                        if (block.getBlockHardness(this.position.world(), t.xi(), t.yi(), t.zi()) >= 0)
                        {
                            power -= this.callBack.getResistance(this.position.world(), position, t, source, block);

                            if (power > 0f)
                            {
                                this.results.add(t.toPos());
                            }
                        }
                    }
                    t = t.add(delta);
                }
            }
        }

        super.run();
    }
}
