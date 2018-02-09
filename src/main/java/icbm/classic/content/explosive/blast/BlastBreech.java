package icbm.classic.content.explosive.blast;

import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
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
        if (!this.world().isRemote)
        {
            EnumFacing direction = EnumFacing.DOWN;
            if (this.exploder instanceof IRotatable)
            {
                direction = ((IRotatable) this.exploder).getDirection().getOpposite();
            }

            this.world().playSound(position.x(), position.y(), position.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);

            float energy = 40 * 2 + depth * 3; //TODO do not hard
            for (int i = 0; i < this.depth; i++)
            {
                Pos dir = new Pos(direction).multiply(i);
                for (int h = -1; h < 2; h++)
                {
                    for (int w = -1; w < 2; w++)
                    {
                        Pos p;
                        if (direction == EnumFacing.DOWN || direction == EnumFacing.UP)
                        {
                            p = dir.add(h, 0, w);
                        }
                        else if (direction == EnumFacing.EAST || direction == EnumFacing.WEST)
                        {
                            p = dir.add(0, h, w);
                        }
                        else if (direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH)
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
                        if (block != Blocks.AIR)
                        {
                            float e = block.getExplosionResistance(world(), p.toBlockPos(), this.exploder, this);
                            if (e < 40)
                            {
                                energy -= e;
                                blownBlocks.add(p.toBlockPos());
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
