package icbm.classic.content.blast.cluster;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.content.blast.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.blast.imp.BlastBase;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.transform.RotationHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class BlastCluster extends BlastBase {

    private static final Vec3d SOUTH_VEC = new Vec3d(0, 0, 1);
    private static final Vec3d UP_VEC = new Vec3d(0, 1, 0);
    private static final float stackScale = 0.1f;
    private static final float offsetScale = 0.25f;

    @Getter
    @Setter
    private BiFunction<Integer, World, Entity> projectileBuilder;

    @Getter
    @Setter
    private float ejectionScale = 0.3f;

    /**
     * Number of droplets to spawn
     */
    @Getter
    @Setter
    private int projectilesToSpawn = 0;

    /**
     * Number of droplets per ejection disc
     */
    @Getter
    @Setter
    private int projectilesPerLayer = 10;

    /**
     * Offset to apply to projectiles from their starting motion vector
     */
    @Getter
    @Setter
    private float motionPitchOffset = -22.5f;

    /**
     * Offset to apply to yaw of each disc after the first
     */
    @Getter
    @Setter
    private float discYawOffset = 22.5f;

    /**
     * Motion vector scaling
     */
    @Getter
    @Setter
    private float motionScale = 0.3f;

    /**
     * Motion vector scaling per layer after first
     */
    @Getter
    @Setter
    private float motionScaleLayer = 0.1f;

    /**
     * Random amount to apply to motion
     */
    @Getter
    @Setter
    private float motionRandomScale = 0.1f;

    /**
     * Initial heading of the blast
     */
    @Getter
    @Setter
    private float sourceYaw = 0;

    /**
     * Initial heading of the blast
     */
    @Getter
    @Setter
    private float sourcePitch = 0;

    @Override
    public IBlastInit setBlastSource(Entity entity) {
        this.sourceYaw = entity.rotationYaw;
        this.sourcePitch = entity.rotationPitch;
        return this;
    }

    @Override
    protected BlastResponse triggerBlast() {
        if (!world().isRemote) {
            boolean spawnedSomething = spawnEmptyMissile();

            final float yawAmount = 360.0f / projectilesPerLayer;

            int bombsToFire = this.projectilesToSpawn;
            int discIndex = 0;
            while (bombsToFire > 0) {

                // Generate outwards disc
                for (int bombIndex = 0; bombIndex < this.projectilesPerLayer && bombsToFire > 0; bombIndex++) {
                    // Decrease count
                    bombsToFire -= 1;

                    // calculate yaw
                    final double yaw = MathHelper.wrapDegrees(yawAmount * bombIndex + (discIndex * discYawOffset));
                    final Vec3d offsetYaw = RotationHelper.rotateY(SOUTH_VEC, yaw);
                    final Vec3d sourceVec = RotationHelper.rotateY(RotationHelper.rotateX(offsetYaw, sourcePitch - 90), sourceYaw);

                    // Calculate back motion to have projectiles shoot at an angle rather than direct 90
                    final Vec3d backVector = RotationHelper.rotateX(UP_VEC, motionPitchOffset);

                    // set base motion
                    double motionX = (sourceVec.x + backVector.x) * motionScale;
                    double motionY = (sourceVec.y + backVector.y) * motionScale;
                    double motionZ = (sourceVec.z + backVector.z) * motionScale;
                    // Increase motion by layers to prevent each layer hitting the same target
                    motionX += (sourceVec.x + backVector.x) * motionScaleLayer * discIndex;
                    motionY += (sourceVec.y + backVector.y) * motionScaleLayer * discIndex;
                    motionZ += (sourceVec.z + backVector.z) * motionScaleLayer * discIndex;

                    // Randomize motion to create a less perfect pattern
                    if(motionRandomScale > 0) {
                        motionX += (world().rand.nextFloat() - world().rand.nextFloat()) * motionScaleLayer;
                        motionY += (world().rand.nextFloat() - world().rand.nextFloat()) * motionScaleLayer;
                        motionZ += (world().rand.nextFloat() - world().rand.nextFloat()) * motionScaleLayer;
                    }

                    // set position to slightly next to missile body
                    final Vec3d stackVector = RotationHelper.rotateY(RotationHelper.rotateX(SOUTH_VEC, sourcePitch - 180), sourceYaw); //offset from disc, if facing down this is y++
                    double x = (sourceVec.x * offsetScale) + (stackVector.x * stackScale * discIndex);
                    double y = (sourceVec.y * offsetScale) + (stackVector.y * stackScale * discIndex);
                    double z = (sourceVec.z * offsetScale) + (stackVector.z * stackScale * discIndex);


                    //TODO confirm we spawned at least 1
                    spawnedSomething = spawnProjectile(bombsToFire, x, y, z, motionX, motionY, motionZ) || spawnedSomething;
                }

                // Move to next layer
                discIndex += 1;
            }

            return spawnedSomething ? BlastState.TRIGGERED.genericResponse : BlastForgeResponses.ENTITY_SPAWNING.get();
        }
        return BlastState.TRIGGERED.genericResponse;
    }

    private boolean spawnProjectile(int index, double x, double y, double z, double mx, double my, double mz) {
        final Entity entity = projectileBuilder != null ? projectileBuilder.apply(index, world()) : null;
        if(entity != null) {

            entity.setPosition(x() + x, y() + y, z() + z);

            entity.motionX = mx;
            entity.motionY = my;
            entity.motionZ = mz;

            // set rotation to match motion
            final float f3 = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
            entity.prevRotationYaw = entity.rotationYaw = (float) (Math.atan2(entity.motionX, entity.motionZ) * 180.0D / Math.PI);
            entity.prevRotationPitch = entity.rotationPitch = (float) (Math.atan2(entity.motionY, f3) * 180.0D / Math.PI);

            return world().spawnEntity(entity);
        }
        return false;
    }

    private boolean spawnEmptyMissile() {
        // Using bomblet for placeholder until we make an empty missile body
        EntityBombDroplet bomblet = new EntityBombDroplet(world());
        bomblet.explosive.setStack(new ItemStack(ItemReg.itemBomblet));
        bomblet.setPosition(x(), y(), z());
        bomblet.initAimingPosition(
            x(),
            y(),
            z(),
            (float) sourceYaw,
            (float) sourcePitch,
            0f, 0.03f);
        return world().spawnEntity(bomblet);
    }
}
