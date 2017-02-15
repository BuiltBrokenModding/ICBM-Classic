package icbm.classic.content.explosive.blast;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.core.References;
import com.builtbroken.mc.lib.transform.vector.Pos;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.explosive.thread.ThreadSmallExplosion;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
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
        if (!this.world().isRemote)
        {
            this.thread = new ThreadSmallExplosion(this.position, (int) this.getRadius(), this.exploder);
            this.thread.start();
        }

        this.world().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public void doExplode()
    {
        int r = this.callCount;

        if (!this.world().isRemote && this.thread.isComplete)
        {
            if (r == 0)
            {
                Collections.sort(this.thread.results, new GravitationalBlockSorter(position));
            }
            int blocksToTake = 20;

            for (Pos targetPosition : this.thread.results)
            {
                double distance = targetPosition.distance(position);

                if (distance > r || distance < r - 2 || blocksToTake <= 0)
                {
                    continue;
                }

                final Block block = targetPosition.getBlock(world());
                if (block != null)
                {
                    float hardness = block.getBlockHardness(world(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi());
                    if (hardness >= 0 && hardness < 1000)
                    {
                        int metadata = world().getBlockMetadata(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                        if (distance < r - 1 || world().rand.nextInt(3) > 0)
                        {
                            //Remove block
                            this.world().setBlockToAir(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());
                            blocksToTake--;

                            //Create flying block
                            EntityFlyingBlock entity = new EntityFlyingBlock(world(), targetPosition.add(0.5D), block, metadata, 0);
                            entity.yawChange = 50 * world().rand.nextFloat();
                            entity.pitchChange = 100 * world().rand.nextFloat();
                            entity.motionY += Math.max(0.15 * world().rand.nextFloat(), 0.1);
                            entity.noClip = true;
                            world().spawnEntityInWorld(entity);

                            //Track flying block
                            flyingBlocks.add(entity);
                        }
                    }
                }
            }
        }

        int radius = (int) this.getRadius();
        AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(position.x() - radius, position.y() - radius, position.z() - radius, position.y() + radius, 100, position.z() + radius);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, bounds);

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

    public class GravitationalBlockSorter implements Comparator<IPos3D>
    {
        final IPos3D center;

        public GravitationalBlockSorter(IPos3D center)
        {
            this.center = center;
        }

        @Override
        public int compare(IPos3D o1, IPos3D o2)
        {
            if ((int) o1.y() == (int) o2.y())
            {
                double d = new Pos(o1).distance(center);
                double d2 = new Pos(o2).distance(center);
                return d > d2 ? 1 : d == d2 ? 0 : -1;
            }
            return Integer.compare((int) o1.y(), (int) o2.y());
        }
    }
}
