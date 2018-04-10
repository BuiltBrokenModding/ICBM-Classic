package icbm.classic.content.explosive.blast;

import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityExplosion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlastAntimatter extends Blast
{
    private boolean destroyBedrock;

    public BlastAntimatter(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastAntimatter(World world, Entity entity, double x, double y, double z, float size, boolean destroyBedrock)
    {
        this(world, entity, x, y, z, size);
        this.destroyBedrock = destroyBedrock;
    }

    /** Called before an explosion happens */
    @Override
    public void doPreExplode()
    {
        super.doPreExplode();
        ICBMSounds.ANTIMATTER.play(world, this.position.x(), this.position.y(), this.position.z(), 7F, (float) (this.world().rand.nextFloat() * 0.1 + 0.9F), true);
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    public void doExplode()
    {
        if (!this.world().isRemote)
        {
            for (int x = (int) -this.getBlastRadius(); x < this.getBlastRadius(); x++)
            {
                for (int y = (int) -this.getBlastRadius(); y < this.getBlastRadius(); y++)
                {
                    for (int z = (int) -this.getBlastRadius(); z < this.getBlastRadius(); z++)
                    {
                        final BlockPos blockPos = new BlockPos(position.xi() + x, position.yi() + y, position.zi() + z);
                        final double dist = position.distance(blockPos);

                        if (dist < this.getBlastRadius())
                        {
                            IBlockState blockState = world.getBlockState(blockPos);

                            if (!blockState.getBlock().isAir(blockState, world, blockPos))
                            {
                                if (!this.destroyBedrock && blockState.getBlockHardness(this.world(), blockPos) < 0)
                                {
                                    continue;
                                }

                                if (dist < this.getBlastRadius() - 1 || world().rand.nextFloat() > 0.7)
                                {
                                    world.setBlockToAir(blockPos);
                                }
                            }
                        }
                    }

                }
            }
        }

        // TODO: Render antimatter shockwave
        /*
         * else if (ZhuYao.proxy.isGaoQing()) { for (int x = -this.getRadius(); x <
         * this.getRadius(); x++) { for (int y = -this.getRadius(); y < this.getRadius(); y++) { for
         * (int z = -this.getRadius(); z < this.getRadius(); z++) { Vector3 targetPosition =
         * Vector3.add(position, new Vector3(x, y, z)); double distance =
         * position.distanceTo(targetPosition);
         * if (targetPosition.getBlockID(worldObj) == 0) { if (distance < this.getRadius() &&
         * distance > this.getRadius() - 1 && worldObj.rand.nextFloat() > 0.5) {
         * ParticleSpawner.spawnParticle("antimatter", worldObj, targetPosition); } } } } } }
         */
    }

    @Override
    public void doPostExplode()
    {
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityExplosion)
        {
            if (((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
            {
                entity.setDead();
                return true;
            }
        }

        return false;
    }
}
