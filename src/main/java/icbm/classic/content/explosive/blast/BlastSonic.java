package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.TileEntityExplosive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class BlastSonic extends Blast
{
    private float energy;
    private ThreadLargeExplosion thread;
    private boolean hasShockWave = false;

    public BlastSonic(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastSonic(World world, Entity entity, double x, double y, double z, float size, float energy)
    {
        this(world, entity, x, y, z, size);
        this.energy = energy;
    }

    public Blast setShockWave()
    {
        this.hasShockWave = true;
        return this;
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            /*
            if (this.hasShockWave)
            {
                for (int x = (int) (-this.getRadius() * 2); x < this.getRadius() * 2; ++x)
                {
                    for (int y = (int) (-this.getRadius() * 2); y < this.getRadius() * 2; ++y)
                    {
                        for (int z = (int) (-this.getRadius() * 2); z < this.getRadius() * 2; ++z)
                        {
                            Location targetPosition = position.add(new Pos(x, y, z));
                            Block blockID = world().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                            if (blockID != Blocks.air)
                            {
                                Material material = blockID.getMaterial();

                                if (blockID != Blocks.bedrock && !(material.isLiquid()) && (blockID.getExplosionResistance(this.exploder, world(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), position.xi(), position.yi(), position.zi()) > this.power || material == Material.glass))
                                {
                                    targetPosition.setBlock(world(), Blocks.air);
                                }
                            }
                        }
                    }
                }
            } */

            this.thread = new ThreadLargeExplosion(this, (int) this.getBlastRadius(), this.energy, this.exploder);
            this.thread.start();
        }

        if (this.hasShockWave)
        {
            ICBMSounds.HYPERSONIC.play(world, position.x(), position.y(), position.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
        else
        {
            ICBMSounds.SONICWAVE.play(world, position.x(), position.y(), position.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public void doExplode()
    {
        int r = this.callCount;

        if (!this.world().isRemote)
        {
            if (this.thread != null && this.thread.isComplete)
            {
                Iterator<BlockPos> it = this.thread.results.iterator();

                while (it.hasNext())
                {
                    BlockPos targetPosition = it.next();
                    double distance = position.distance(targetPosition);

                    if (distance > r || distance < r - 3)
                    {
                        continue;
                    }

                    final IBlockState blockState = world.getBlockState(targetPosition);
                    final Block block = blockState.getBlock();

                    if (block == Blocks.AIR || blockState.getBlockHardness(world, targetPosition) < 0)
                    {
                        continue;
                    }

                    if (distance < r - 1 || this.world().rand.nextInt(3) > 0)
                    {
                        if (block == ICBMClassic.blockExplosive)
                        {
                            BlockExplosive.triggerExplosive(this.world(), targetPosition, ((TileEntityExplosive) this.world().getTileEntity(targetPosition)).explosive, 1);
                        }
                        else
                        {
                            this.world().setBlockToAir(targetPosition);
                        }

                        if (this.world().rand.nextFloat() < 0.3 * (this.getBlastRadius() - r))
                        {
                            EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), targetPosition, blockState);
                            this.world().spawnEntity(entity);
                            entity.yawChange = 50 * this.world().rand.nextFloat();
                            entity.pitchChange = 100 * this.world().rand.nextFloat();
                        }

                        it.remove();
                    }
                }
            }
        }

        int radius = 2 * this.callCount;
        AxisAlignedBB bounds = new AxisAlignedBB(position.x() - radius, position.y() - radius, position.z() - radius, position.x() + radius, position.y() + radius, position.z() + radius);
        List<Entity> allEntities = this.world().getEntitiesWithinAABB(Entity.class, bounds);

        synchronized (allEntities)
        {
            for (Iterator it = allEntities.iterator(); it.hasNext(); )
            {
                Entity entity = (Entity) it.next();

                if (entity instanceof EntityMissile)
                {
                    ((EntityMissile) entity).setExplode();
                    break;
                }
                else
                {
                    double xDifference = entity.posX - position.x();
                    double zDifference = entity.posZ - position.z();

                    r = (int) this.getBlastRadius();
                    if (xDifference < 0)
                    {
                        r = (int) -this.getBlastRadius();
                    }

                    entity.motionX += (r - xDifference) * 0.02 * this.world().rand.nextFloat();
                    entity.motionY += 3 * this.world().rand.nextFloat();

                    r = (int) this.getBlastRadius();
                    if (zDifference < 0)
                    {
                        r = (int) -this.getBlastRadius();
                    }

                    entity.motionZ += (r - zDifference) * 0.02 * this.world().rand.nextFloat();
                }
            }
        }

        if (this.callCount > this.getBlastRadius())
        {
            this.controller.endExplosion();
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
        return 4;
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
