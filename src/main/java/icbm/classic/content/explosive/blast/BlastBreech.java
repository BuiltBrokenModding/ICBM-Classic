package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mffs.api.IForceFieldBlock;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlastBreech extends BlastRepulsive
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
        this.nengLiang = 13;
    }

    @Override
    public void doExplode()
    {
        if (!this.world().isRemote)
        {
            Pos difference = new Pos();

            if (this.exploder instanceof IRotatable)
            {
                difference = difference.add(((IRotatable) this.exploder).getDirection());
            }
            else
            {
                difference = difference.add(ForgeDirection.DOWN);
            }

            this.world().playSoundEffect(position.x(), position.y(), position.z(), "random.explode", 5.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);

            for (int i = 0; i < this.depth; i++)
            {
                if (position.getBlock() != Blocks.air)
                {
                    if (position.getBlock().getExplosionResistance(this.exploder, world(), position.xi(), position.yi(), position.zi(), position.x(), position.y(), position.z()) > Blocks.obsidian.getExplosionResistance(this.exploder) || position.getBlock(world()) instanceof IForceFieldBlock)
                    {
                        break;
                    }
                }

                super.doExplode();
                this.position.add(difference);
            }
        }
    }

    @Override
    public long getEnergy()
    {
        return (super.getEnergy() * this.depth) / 2;
    }
}
