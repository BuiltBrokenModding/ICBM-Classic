package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlastBreech extends BlastTNT
{
    private int depth;

    public BlastBreech(World world, Entity entity, double x, double y, double z, float size, int depth)
    {
        this(world, entity, x, y, z, size);
        this.depth = depth;
    }

    public BlastBreech(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
        this.power = 13;
    }

    @Override
    protected void calculateDamage()
    {
        if (!this.world().isRemote)
        {
            ForgeDirection direction = ForgeDirection.DOWN;
            if (this.exploder instanceof IRotatable)
            {
                direction = ((IRotatable) this.exploder).getDirection();
            }

            this.world().playSoundEffect(position.x(), position.y(), position.z(), "random.explode", 5.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);

            float energy = 40 * 2 + depth * 3; //TODO do not hard
            for (int i = 0; i < this.depth; i++)
            {
                Pos dir = new Pos(direction).multiply(i);
                for (int h = -1; h < 2; h++)
                {
                    for (int w = -1; w < 2; w++)
                    {
                        Pos p;
                        if (direction == ForgeDirection.DOWN || direction == ForgeDirection.UP)
                        {
                            p = dir.add(h, 0, w);
                        }
                        else if (direction == ForgeDirection.EAST || direction == ForgeDirection.WEST)
                        {
                            p = dir.add(0, h, w);
                        }
                        else if (direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH)
                        {
                            p = dir.add(w, h, 0);
                        }
                        else
                        {
                            return;
                        }

                        //Translate by center
                        p = toPos().add(p);

                        Block block = p.getBlock(world());
                        if (block != Blocks.air)
                        {
                            float e = block.getExplosionResistance(this.exploder, world(), p.xi(), p.yi(), p.zi(), position.x(), position.y(), position.z());
                            if (e < 40)
                            {
                                energy -= e;
                                blownBlocks.add(p);
                            }
                        }
                        if (energy <= 0)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public long getEnergy()
    {
        return (super.getEnergy() * this.depth) / 2;
    }
}
