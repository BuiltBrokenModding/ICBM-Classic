package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlastEnderman extends Blast
{
    public int duration = 20 * 8;
    private Pos teleportTarget;

    public BlastEnderman(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastEnderman(World world, Entity entity, double x, double y, double z, float size, Pos teleportTarget)
    {
        super(world, entity, x, y, z, size);
        this.teleportTarget = teleportTarget;
    }

    @Override
    public void doExplode()
    {
        if (this.oldWorld().isRemote)
        {
            int r = (int) (this.getRadius() - ((double) this.callCount / (double) this.duration) * this.getRadius());

            for (int x = -r; x < r; x++)
            {
                for (int z = -r; z < r; z++)
                {
                    for (int y = -r; y < r; y++)
                    {
                        Location targetPosition = position.add(new Pos(x, y, z));

                        double distance = targetPosition.distance(position);

                        if (distance < r && distance > r - 1)
                        {
                            if (targetPosition.getBlock(oldWorld()) != Blocks.AIR)
                            {
                                continue;
                            }

                            if (this.oldWorld().rand.nextFloat() < Math.max(0.001 * r, 0.01))
                            {
                                float velX = (float) ((targetPosition.x() - position.x()) * 0.6);
                                float velY = (float) ((targetPosition.y() - position.y()) * 0.6);
                                float velZ = (float) ((targetPosition.z() - position.z()) * 0.6);

                                world.spawnParticle(EnumParticleTypes.PORTAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), velX, velY, velZ);
                            }
                        }
                    }
                }
            }
        }

        int radius = (int) this.getRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(position.x() - radius, position.y() - radius, position.z() - radius, position.x() + radius, position.y() + radius, position.z() + radius);
        List<Entity> allEntities = oldWorld().getEntitiesWithinAABB(Entity.class, bounds);
        boolean explosionCreated = false;

        for (Entity entity : allEntities)
        {
            if (entity != this.controller)
            {

                double xDifference = entity.posX - position.x();
                double yDifference = entity.posY - position.y();
                double zDifference = entity.posZ - position.z();

                int r = (int) this.getRadius();
                if (xDifference < 0)
                {
                    r = (int) -this.getRadius();
                }

                entity.motionX -= (r - xDifference) * Math.abs(xDifference) * 0.0006;

                r = (int) this.getRadius();
                if (entity.posY > position.y())
                {
                    r = (int) -this.getRadius();
                }
                entity.motionY += (r - yDifference) * Math.abs(yDifference) * 0.0011;

                r = (int) this.getRadius();
                if (zDifference < 0)
                {
                    r = (int) -this.getRadius();
                }

                entity.motionZ -= (r - zDifference) * Math.abs(zDifference) * 0.0006;

                if (new Pos(entity.posX, entity.posY, entity.posZ).distance(position) < 4)
                {
                    if (!explosionCreated && callCount % 5 == 0)
                    {
                        oldWorld().spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, entity.posX, entity.posY, entity.posZ, 0.0D, 0.0D, 0.0D);
                        explosionCreated = true;
                    }

                    try
                    {
                        /** If a target doesn't exist, search for a random one within 100 block
                         * range. */
                        if (this.teleportTarget == null)
                        {
                            int checkY = (int) Math.floor(this.controller.posY);
                            int checkX = this.oldWorld().rand.nextInt(300) - 150 + (int) this.controller.posX;
                            int checkZ = this.oldWorld().rand.nextInt(300) - 150 + (int) this.controller.posZ;

                            //Look for space with air gap
                            BlockPos pos;
                            BlockPos pos2;
                            do
                            {
                                pos = new BlockPos(checkX, checkY, checkZ);
                                pos2 = pos.up();
                                checkY++;
                            }
                            while (this.oldWorld().isAirBlock(pos) && !this.oldWorld().isAirBlock(pos2) && checkY < 254);

                            this.teleportTarget = new Pos(checkX, checkY, checkZ);
                        }

                        this.oldWorld().playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

                        if (entity instanceof EntityPlayerMP)
                        {
                            ((EntityPlayerMP) entity).connection.setPlayerLocation(this.teleportTarget.x() + 0.5, this.teleportTarget.y() + 0.5, this.teleportTarget.z() + 0.5, entity.rotationYaw, entity.rotationPitch);
                        }
                        else
                        {
                            entity.setPosition(this.teleportTarget.x() + 0.5, this.teleportTarget.y() + 0.5, this.teleportTarget.z() + 0.5);
                        }

                    }
                    catch (Exception e)
                    {
                        ICBMClassic.INSTANCE.logger().error("Failed to teleport entity to the End.", e);
                    }
                }
            }
        }

        this.oldWorld().playSound(this.position.x(), this.position.y(), this.position.z(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 2F, oldWorld().rand.nextFloat() * 0.4F + 0.8F, false);

        if (this.callCount > this.duration)
        {
            this.controller.endExplosion();
        }
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.oldWorld().isRemote)
        {
            for (int i = 0; i < 8; i++)
            {
                EntityEnderman enderman = new EntityEnderman(oldWorld());
                enderman.setPosition(this.position.x(), this.position.y(), this.position.z());
                this.oldWorld().spawnEntity(enderman);
            }
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
        return 20;
    }

    @Override
    public long getEnergy()
    {
        return 0;
    }

    @Override
    public boolean isMovable()
    {
        return true;
    }
}
