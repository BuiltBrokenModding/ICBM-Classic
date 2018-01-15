package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

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
        this.damageToEntities = 13;
    }

    @Override
    protected void calculateDamage()
    {
        if (!this.oldWorld().isRemote)
        {
            Direction direction = Direction.DOWN;
            if (this.exploder instanceof IRotatable)
            {
                direction = ((IRotatable) this.exploder).getDirection();
            }

            this.oldWorld().playSound(position.x(), position.y(), position.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F, true);

            float energy = 40 * 2 + depth * 3; //TODO do not hard
            for (int i = 0; i < this.depth; i++)
            {
                Pos dir = new Pos(direction).multiply(i);
                for (int h = -1; h < 2; h++)
                {
                    for (int w = -1; w < 2; w++)
                    {
                        Pos p;
                        if (direction == Direction.DOWN || direction == Direction.UP)
                        {
                            p = dir.add(h, 0, w);
                        }
                        else if (direction == Direction.EAST || direction == Direction.WEST)
                        {
                            p = dir.add(0, h, w);
                        }
                        else if (direction == Direction.NORTH || direction == Direction.SOUTH)
                        {
                            p = dir.add(w, h, 0);
                        }
                        else
                        {
                            return;
                        }

                        //Translate by center
                        p = toPos().add(p);

                        Block block = p.getBlock(oldWorld());
                        if (block != Blocks.AIR)
                        {
                            float e = block.getExplosionResistance(oldWorld(), p.toBlockPos(), this.exploder, this);
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
