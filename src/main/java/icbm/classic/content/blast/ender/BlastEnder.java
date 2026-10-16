package icbm.classic.content.blast.ender;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.List;

public class BlastEnder extends Blast implements IBlastTickable //TODO handle save/load
{
    public int duration = 20 * 8;

    @Getter
    @Setter
    private Vec3 teleportTarget;

    /** PORT: dimension of {@link #teleportTarget}, 1.12 stored it on the customization but never used it. Null for the blast's own level. */
    @Getter
    @Setter
    private ResourceKey<Level> teleportDim;

    @Override
    public boolean doExplode(int callCount) //TODO break into smaller methods
    {
        if (this.world().isClientSide())
        {
            int r = (int) (this.getBlastRadius() - ((double) this.callCount / (double) this.duration) * this.getBlastRadius());

            for (int x = -r; x < r; x++)
            {
                for (int z = -r; z < r; z++)
                {
                    for (int y = -r; y < r; y++)
                    {
                        Location targetPosition = location.add(new Pos(x, y, z)); //TODO replace with mutable blockpos

                        double distance = targetPosition.distance(location);

                        if (distance < r && distance > r - 1)
                        {
                            if (!targetPosition.getBlockState(world()).isAir())
                            {
                                continue;
                            }

                            if (this.world().random.nextFloat() < Math.max(0.001 * r, 0.01))
                            {
                                float velX = (float) ((targetPosition.x() - location.x()) * 0.6);
                                float velY = (float) ((targetPosition.y() - location.y()) * 0.6);
                                float velZ = (float) ((targetPosition.z() - location.z()) * 0.6);

                                world.addParticle(ParticleTypes.PORTAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), velX, velY, velZ);
                            }
                        }
                    }
                }
            }
        }

        int radius = (int) this.getBlastRadius();
        AABB bounds = new AABB(location.x() - radius, location.y() - radius, location.z() - radius, location.x() + radius, location.y() + radius, location.z() + radius);
        List<Entity> allEntities = world().getEntitiesOfClass(Entity.class, bounds);
        boolean explosionCreated = false;

        for (Entity entity : allEntities)
        {
            if (entity != this.controller)
            {

                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();
                double motionX = 0, motionY = 0, motionZ = 0;

                int r = (int) this.getBlastRadius();
                if (xDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                motionX -= (r - xDifference) * Math.abs(xDifference) * 0.0006;

                r = (int) this.getBlastRadius();
                if (entity.getY() > location.y())
                {
                    r = (int) -this.getBlastRadius();
                }
                motionY += (r - yDifference) * Math.abs(yDifference) * 0.0011;

                r = (int) this.getBlastRadius();
                if (zDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                motionZ -= (r - zDifference) * Math.abs(zDifference) * 0.0006;
                entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
                markVelocityChanged(entity);

                if (new Pos(entity.getX(), entity.getY(), entity.getZ()).distance(location) < 4)
                {
                    if (!explosionCreated && callCount % 5 == 0)
                    {
                        // PORT: 1.12 spawned the particle with World#spawnParticle, which does nothing on the server
                        if (world() instanceof ServerLevel serverLevel)
                        {
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY(), entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        explosionCreated = true;
                    }

                    try
                    {
                        // If a target doesn't exist, search for a random one within 100 block range
                        if (this.teleportTarget == null)
                        {
                            final double centerX = this.controller != null ? this.controller.getX() : location.x();
                            final double centerZ = this.controller != null ? this.controller.getZ() : location.z();
                            final int checkX = this.world().random.nextInt(300) - 150 + (int) centerX;
                            final int checkZ = this.world().random.nextInt(300) - 150 + (int) centerZ;

                            // PORT: 1.12 searched up from the blast y while the block was air and the block above was
                            //  solid, which stopped on the first block and left targets inside terrain or in the air.
                            //  Uses the surface instead, loading or generating the chunk first so the height is known.
                            final LevelChunk chunk = this.world().getChunk(SectionPos.blockToSectionCoord(checkX), SectionPos.blockToSectionCoord(checkZ));
                            int topY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, checkX & 15, checkZ & 15) + 1;

                            // Surface of a level with a roof (nether) is the roof, look for a two block gap below the blast instead
                            if (this.world().dimensionType().hasCeiling())
                            {
                                final int minY = this.world().getMinBuildHeight();
                                final int startY = Math.min(location.yi(), minY + this.world().dimensionType().logicalHeight() - 2);
                                final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(checkX, startY, checkZ);
                                for (; pos.getY() > minY; pos.move(Direction.DOWN))
                                {
                                    final BlockPos below = pos.below();
                                    if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(pos.above()).isAir()
                                        && chunk.getBlockState(below).isFaceSturdy(chunk, below, Direction.UP))
                                    {
                                        topY = pos.getY();
                                        break;
                                    }
                                }
                            }

                            this.teleportTarget = new Vec3(checkX + 0.5, topY, checkZ + 0.5);
                            this.teleportDim = null; // random target is always in this level
                        }

                        this.world().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);

                        final ServerLevel targetLevel = getTeleportLevel();
                        if (targetLevel != null)
                        {
                            // PORT: 1.12 ignored the dimension of the target and teleported inside the current one
                            entity.changeDimension(new DimensionTransition(targetLevel, this.teleportTarget, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
                        }
                        else if (entity instanceof ServerPlayer)
                        {
                            ((ServerPlayer) entity).connection.teleport(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z, entity.getYRot(), entity.getXRot());
                        }
                        else
                        {
                            entity.setPos(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z);
                        }

                    } catch (Exception e)
                    {
                        ICBMClassic.logger().error("Failed to teleport entity to the End.", e);
                    }
                }
            }
        }

        this.world().playSound(null, this.location.x(), this.location.y(), this.location.z(), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 2F, world().random.nextFloat() * 0.4F + 0.8F);

        return this.callCount > this.duration;
    }

    /**
     * @return level to move entities to when the target is in another dimension, null to teleport inside this level
     */
    @Nullable
    private ServerLevel getTeleportLevel()
    {
        if (teleportDim != null && world() instanceof ServerLevel serverLevel && !teleportDim.equals(serverLevel.dimension()))
        {
            return serverLevel.getServer().getLevel(teleportDim);
        }
        return null;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        if (!this.world().isClientSide())
        {
            for (int i = 0; i < 8; i++) //TODO check for safe location to spawn
            {
                EnderMan enderman = new EnderMan(EntityType.ENDERMAN, world());
                enderman.setPos(this.location.x(), this.location.y(), this.location.z());
                this.world().addFreshEntity(enderman);
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
// PORTED
