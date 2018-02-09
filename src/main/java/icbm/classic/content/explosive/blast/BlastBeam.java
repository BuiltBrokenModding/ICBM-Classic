package icbm.classic.content.explosive.blast;

import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.content.explosive.thread.ThreadExplosion;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

/** Used by Exothermic and Endothermic explosions.
 *
 * @author Calclavia */
public abstract class BlastBeam extends Blast
{
    protected ThreadExplosion thread;
    protected Set<EntityFlyingBlock> feiBlocks = new HashSet<EntityFlyingBlock>();
    protected EntityLightBeam lightBeam;
    protected float red, green, blue;
    /** Radius in which the uplighting of blocks takes place */
    protected int radius = 5;

    public BlastBeam(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    /** Called before an explosion happens */
    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            this.world().createExplosion(this.exploder, position.x(), position.y(), position.z(), 4F, true);

            this.lightBeam = new EntityLightBeam(this.world(), position, 20 * 20, this.red, this.green, this.blue);
            this.world().spawnEntity(this.lightBeam);

            this.thread = new ThreadLargeExplosion(this, (int) this.getRadius(), 50, this.exploder);
            this.thread.start();
        }
    }

    @Override
    public void doExplode()
    {
        if (!this.world().isRemote)
        {
            if (this.callCount > 100 / this.proceduralInterval() && this.thread.isComplete)
            {
                this.controller.endExplosion();
            }

            if (this.canFocusBeam(this.world(), position))
            {
                double dist;

                int r = radius;

                for (int x = -r; x < r; x++)
                {
                    for (int y = -r; y < r; y++)
                    {
                        for (int z = -r; z < r; z++)
                        {
                            dist = MathHelper.sqrt((x * x + y * y + z * z));

                            if (dist > r || dist < r - 3)
                            {
                                continue;
                            }
                            BlockPos blockPos = new BlockPos(position.x() + x, position.y() + y, position.z() + z);
                            IBlockState state = world.getBlockState(blockPos);
                            Block block = state.getBlock();
                            if (block == null || block.isAir(state, world, blockPos) || state.getBlockHardness(world, blockPos) < 0)
                            {
                                continue;
                            }
                            if (this.world().rand.nextInt(2) > 0)
                            {
                                world.setBlockToAir(blockPos);
                                EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), blockPos, state);
                                this.world().spawnEntity(entity);
                                this.feiBlocks.add(entity);
                                entity.pitchChange = 50 * this.world().rand.nextFloat();
                            }
                        }
                    }
                }
            }
            else
            {
                this.controller.endExplosion();
            }

            for (EntityFlyingBlock entity : this.feiBlocks)
            {
                Pos entityPosition = new Pos(entity);
                Pos centeredPosition = entityPosition.add(this.position.multiply(-1));
                centeredPosition.rotate(2);
                Location newPosition = this.position.add(centeredPosition);
                entity.motionX /= 3;
                entity.motionY /= 3;
                entity.motionZ /= 3;
                entity.addVelocity((newPosition.x() - entityPosition.x()) * 0.5 * this.proceduralInterval(), 0.09 * this.proceduralInterval(), (newPosition.z() - entityPosition.z()) * 0.5 * this.proceduralInterval());
                entity.yawChange += 3 * this.world().rand.nextFloat();
            }
        }
    }

    @Override
    public void doPostExplode()
    {
        if (!this.world().isRemote)
        {
            if (this.lightBeam != null)
            {
                this.lightBeam.setDead();
                this.lightBeam = null;
            }
        }
    }

    public boolean canFocusBeam(World worldObj, Location position)
    {
        return position.canSeeSky();
    }

    /** The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need proceudral calls */
    @Override
    public int proceduralInterval()
    {
        return 4;
    }

    @Override
    public long getEnergy()
    {
        return 10000;
    }

}
