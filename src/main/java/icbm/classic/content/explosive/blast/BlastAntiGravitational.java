package icbm.classic.content.explosive.blast;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.explosive.thread.ThreadSmallExplosion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class BlastAntiGravitational extends Blast
{
    protected ThreadSmallExplosion thread;
    protected Set<EntityFlyingBlock> flyingBlocks = new HashSet<EntityFlyingBlock>();

    public BlastAntiGravitational(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doPreExplode()
    {
        if (!this.oldWorld().isRemote)
        {
            this.thread = new ThreadSmallExplosion(this, (int) this.getRadius(), this.exploder);
            this.thread.start();
        }

        //this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public void doExplode()
    {
        int r = this.callCount;

        if (!this.oldWorld().isRemote && this.thread.isComplete)
        {
            if (r == 0)
            {
                Collections.sort(this.thread.results, new GravitationalBlockSorter(position));
            }
            int blocksToTake = 20;

            for (BlockPos targetPosition : this.thread.results)
            {
                final IBlockState blockState = world.getBlockState(targetPosition);
                if (blockState.getBlock() != Blocks.AIR)
                {
                    float hardness = blockState.getBlockHardness(world, targetPosition);
                    if (hardness >= 0 && hardness < 1000)
                    {
                        if (oldWorld().rand.nextInt(3) > 0)
                        {
                            //Remove block
                            world.setBlockToAir(targetPosition);

                            //Mark blocks taken
                            blocksToTake--;
                            if (blocksToTake <= 0)
                            {
                                break;
                            }

                            //Create flying block
                            EntityFlyingBlock entity = new EntityFlyingBlock(oldWorld(), targetPosition, blockState, 0);
                            entity.yawChange = 50 * oldWorld().rand.nextFloat();
                            entity.pitchChange = 100 * oldWorld().rand.nextFloat();
                            entity.motionY += Math.max(0.15 * oldWorld().rand.nextFloat(), 0.1);
                            entity.noClip = true;
                            oldWorld().spawnEntity(entity);

                            //Track flying block
                            flyingBlocks.add(entity);
                        }
                    }
                }
            }
        }

        int radius = (int) this.getRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(position.x() - radius, position.y() - radius, position.z() - radius, position.y() + radius, 100, position.z() + radius);
        List<Entity> allEntities = oldWorld().getEntitiesWithinAABB(Entity.class, bounds);

        for (Entity entity : allEntities)
        {
            if (!(entity instanceof EntityFlyingBlock) && entity.posY < 100 + position.y())
            {
                if (entity.motionY < 0.4)
                {
                    entity.motionY += 0.15;
                }
            }
        }

        if (this.callCount > 20 * 120)
        {
            this.controller.endExplosion();
        }
    }

    @Override
    protected void doPostExplode()
    {
        for (EntityFlyingBlock entity : flyingBlocks)
        {
            entity.gravity = 0.045f;
        }
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need proceudral calls
     */
    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    @Override
    public float getRadius()
    {
        return 15;
    }

    @Override
    public long getEnergy()
    {
        return 10000;
    }

    public class GravitationalBlockSorter implements Comparator<BlockPos>
    {
        final IPos3D center;

        public GravitationalBlockSorter(IPos3D center)
        {
            this.center = center;
        }

        @Override
        public int compare(BlockPos o1, BlockPos o2)
        {
            if (o1.getY() == o2.getY())
            {
                double d = new Pos(o1).distance(center);
                double d2 = new Pos(o2).distance(center);
                return d > d2 ? 1 : d == d2 ? 0 : -1;
            }
            return Integer.compare(o1.getY(), o2.getY());
        }
    }
}
