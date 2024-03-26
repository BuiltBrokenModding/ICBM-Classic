package icbm.classic.world.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.IBlastTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EntityEnderman;
import net.minecraft.world.entity.player.PlayerMP;

import javax.annotation.Nonnull;
import java.util.List;

public class BlastEnderman extends Blast implements IBlastTickable {

    public static final String NBT_LOCATION = "teleport_target";
    public int duration = 20 * 8;
    private Pos teleportTarget;

    @Override
    public IBlastInit setCustomData(@Nonnull CompoundTag customData) {
        if (customData != null && customData.contains(NBT_LOCATION)) {
            teleportTarget = new Pos(customData.getCompound(NBT_LOCATION));
            //TODO load world ID
            //TODO data fixer, previous data was store raw as xyz
        }
        return this;
    }

    @Override
    public boolean doExplode(int callCount) {
        if (this.level().isClientSide()) {
            int r = (int) (this.getBlastRadius() - ((double) this.callCount / (double) this.duration) * this.getBlastRadius());

            for (int x = -r; x < r; x++) {
                for (int z = -r; z < r; z++) {
                    for (int y = -r; y < r; y++) {
                        Location targetPosition = location.add(new Pos(x, y, z)); //TODO replace with mutable blockpos

                        double distance = targetPosition.distance(location);

                        if (distance < r && distance > r - 1) {
                            if (!targetPosition.getBlock(level()).isAir(targetPosition.getBlockState(level()), level(), targetPosition.toBlockPos())) {
                                continue;
                            }

                            if (this.level().rand.nextFloat() < Math.max(0.001 * r, 0.01)) {
                                float velX = (float) ((targetPosition.x() - location.x()) * 0.6);
                                float velY = (float) ((targetPosition.y() - location.y()) * 0.6);
                                float velZ = (float) ((targetPosition.z() - location.z()) * 0.6);

                                world.spawnParticle(EnumParticleTypes.PORTAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), velX, velY, velZ);
                            }
                        }
                    }
                }
            }
        }

        int radius = (int) this.getBlastRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius, location.x() + radius, location.y() + radius, location.z() + radius);
        List<Entity> allEntities = level().getEntitiesWithinAABB(Entity.class, bounds);
        boolean explosionCreated = false;

        for (Entity entity : allEntities) {
            if (entity != this.controller) {

                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();

                int r = (int) this.getBlastRadius();
                if (xDifference < 0) {
                    r = (int) -this.getBlastRadius();
                }

                entity.motionX -= (r - xDifference) * Math.abs(xDifference) * 0.0006;

                r = (int) this.getBlastRadius();
                if (entity.getY() > location.y()) {
                    r = (int) -this.getBlastRadius();
                }
                entity.motionY += (r - yDifference) * Math.abs(yDifference) * 0.0011;

                r = (int) this.getBlastRadius();
                if (zDifference < 0) {
                    r = (int) -this.getBlastRadius();
                }

                entity.motionZ -= (r - zDifference) * Math.abs(zDifference) * 0.0006;

                if (new Pos(entity.getX(), entity.getY(), entity.getZ()).distance(location) < 4) {
                    if (!explosionCreated && callCount % 5 == 0) {
                        level().spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
                        explosionCreated = true;
                    }

                    try {
                        /** If a target doesn't exist, search for a random one within 100 block
                         * range. */
                        if (this.teleportTarget == null) {
                            int checkY = (int) Math.floor(this.controller.getY());
                            int checkX = this.level().rand.nextInt(300) - 150 + (int) this.controller.getX();
                            int checkZ = this.level().rand.nextInt(300) - 150 + (int) this.controller.getZ();

                            //Look for space with air gap
                            BlockPos pos;
                            BlockPos pos2;
                            do {
                                pos = new BlockPos(checkX, checkY, checkZ);
                                pos2 = pos.up();
                                checkY++;
                            }
                            while (this.level().isAirBlock(pos) && !this.level().isAirBlock(pos2) && checkY < 254);

                            this.teleportTarget = new Pos(checkX, checkY, checkZ);
                        }

                        this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        if (entity instanceof PlayerMP) {
                            ((PlayerMP) entity).connection.setPlayerLocation(this.teleportTarget.x() + 0.5, this.teleportTarget.y() + 0.5, this.teleportTarget.z() + 0.5, entity.getYRot(), entity.getXRot());
                        } else {
                            entity.setPos(this.teleportTarget.x() + 0.5, this.teleportTarget.y() + 0.5, this.teleportTarget.z() + 0.5);
                        }

                    } catch (Exception e) {
                        ICBMClassic.logger().error("Failed to teleport entity to the End.", e);
                    }
                }
            }
        }

        this.level().playSound(null, this.location.x(), this.location.y(), this.location.z(), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 2F, level().rand.nextFloat() * 0.4F + 0.8F);

        return this.callCount > this.duration;
    }

    @Override
    public void onBlastCompleted() {
        super.onBlastCompleted();

        if (!this.level().isClientSide()) {
            for (int i = 0; i < 8; i++) //TODO check for safe location to spawn
            {
                EntityEnderman enderman = new EntityEnderman(level());
                enderman.setPosition(this.location.x(), this.location.y(), this.location.z());
                this.level().spawnEntity(enderman);
            }
        }
    }

    @Override
    public float getBlastRadius() {
        return 20;
    }

    @Override
    public boolean isMovable() {
        return true;
    }
}
