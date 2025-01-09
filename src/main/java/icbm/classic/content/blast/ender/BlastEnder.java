package icbm.classic.content.blast.ender;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.Blast;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlastEnder extends Blast implements IBlastTickable //TODO handle save/load
{
    public int duration = 20 * 8;

    @Getter
    @Setter
    private Vec3d teleportTarget;

    @Override
    public boolean doExplode(int callCount) //TODO break into smaller methods
    {
        if (this.getWorld().isRemote)
        {
            int r = (int) (this.getBlastRadius() - ((double) this.callCount / (double) this.duration) * this.getBlastRadius());

            for (int x = -r; x < r; x++)
            {
                for (int z = -r; z < r; z++)
                {
                    for (int y = -r; y < r; y++)
                    {
                        final BlockPos targetPosition = new BlockPos(getPosition().add(x, y, z));
                        double distance = targetPosition.distanceSq(new BlockPos(getPosition()));

                        if (distance < r && distance > r - 1)
                        {
                            if (!getWorld().isAirBlock(targetPosition))
                            {
                                continue;
                            }

                            if (this.getWorld().rand.nextFloat() < Math.max(0.001 * r, 0.01))
                            {
                                float velX = (float) ((targetPosition.getX() - pos.getX()) * 0.6); //TODO use blast double positions
                                float velY = (float) ((targetPosition.getY() - pos.getY()) * 0.6);
                                float velZ = (float) ((targetPosition.getZ() - pos.getZ()) * 0.6);

                                world.addParticle(ParticleTypes.PORTAL, targetPosition.getX() + 0.5, targetPosition.getY() + 0.5, targetPosition.getZ() + 0.5, velX, velY, velZ);
                            }
                        }
                    }
                }
            }
        }

        int radius = (int) this.getBlastRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(
            getPosition().x - radius, getPosition().y - radius, getPosition().z - radius,
            getPosition().x + radius, getPosition().y + radius, getPosition().z + radius);
        List<Entity> allEntities = getWorld().getEntitiesWithinAABB(Entity.class, bounds);
        boolean explosionCreated = false;

        for (Entity entity : allEntities)
        {
            if (entity != this.controller)
            {

                double xDifference = entity.posX - getPosition().x;
                double yDifference = entity.posY - getPosition().y;
                double zDifference = entity.posZ - getPosition().z;

                int r = (int) this.getBlastRadius();
                if (xDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                entity.addVelocity(-(r - xDifference) * Math.abs(xDifference) * 0.0006, 0, 0);

                r = (int) this.getBlastRadius();
                if (entity.posY > getPosition().y)
                {
                    r = (int) -this.getBlastRadius();
                }
                entity.addVelocity(0, -(r - yDifference) * Math.abs(yDifference) * 0.0011, 0);

                r = (int) this.getBlastRadius();
                if (zDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                entity.addVelocity(0, 0, -(r - zDifference) * Math.abs(zDifference) * 0.0006);

                //TODO optimize to not use an object for distance check
                if (new Vec3d(entity.posX, entity.posY, entity.posZ).distanceTo(getPosition()) < 4) //TODO magic number
                {
                    if (!explosionCreated && callCount % 5 == 0)
                    {
                        getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER, entity.posX, entity.posY, entity.posZ, 0.0D, 0.0D, 0.0D);
                        explosionCreated = true;
                    }

                    try
                    {
                        // If a target doesn't exist, search for a random one within 100 block range
                        if (this.teleportTarget == null)
                        {
                            int checkY = (int) Math.floor(this.controller.posY);
                            int checkX = getWorld().rand.nextInt(300) - 150 + (int) this.controller.posX;
                            int checkZ = getWorld().rand.nextInt(300) - 150 + (int) this.controller.posZ;

                            //Look for space with air gap
                            BlockPos pos;
                            BlockPos pos2;
                            do
                            {
                                pos = new BlockPos(checkX, checkY, checkZ);
                                pos2 = pos.up();
                                checkY++;
                            }
                            while (getWorld().isAirBlock(pos) && !getWorld().isAirBlock(pos2) && checkY < 254);

                            this.teleportTarget = new Vec3d(checkX + 0.5, checkY + 0.5, checkZ + 0.5);
                        }

                        getWorld().playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        if (entity instanceof ServerPlayerEntity)
                        {
                            ((ServerPlayerEntity) entity).connection.setPlayerLocation(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z, entity.rotationYaw, entity.rotationPitch);
                        }
                        else
                        {
                            entity.setPosition(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z);
                        }

                    } catch (Exception e)
                    {
                        ICBMClassic.logger().error("Failed to teleport entity to the End.", e);
                    }
                }
            }
        }

        getWorld().playSound(null, getPosition().x, getPosition().y, getPosition().z, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 2F, getWorld().rand.nextFloat() * 0.4F + 0.8F);

        return this.callCount > this.duration;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        if (!this.getWorld().isRemote)
        {
            for (int i = 0; i < 8; i++) //TODO check for safe location to spawn
            {
                EndermanEntity enderman = EntityType.ENDERMAN.create(this.getWorld());
                enderman.setPosition(getPosition().x, getPosition().y, getPosition().z);
                getWorld().addEntity(enderman);
            }
        }
    }

    @Override
    public float getBlastRadius()
    {
        return 20;
    }

    @Override
    public boolean isMovable()
    {
        return true;
    }
}
