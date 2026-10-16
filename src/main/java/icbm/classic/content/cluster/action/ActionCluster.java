package icbm.classic.content.cluster.action;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.lib.actions.ActionBase;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.transform.RotationHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class ActionCluster extends ActionBase {

    private static final Vec3 SOUTH_VEC = new Vec3(0, 0, 1);
    private static final Vec3 UP_VEC = new Vec3(0, 1, 0);
    private static final float stackScale = 0.1f;
    private static final float offsetScale = 0.25f;

    @Getter
    @Setter
    private NonNullList<ItemStack> spawnList = NonNullList.create();

    @Getter
    @Setter
    private boolean allowPickupItem = true;

    @Getter
    @Setter
    private float ejectionScale = 0.3f;

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

    public ActionCluster(Level world, Vec3 position, IActionSource source, IActionData actionData) {
        super(world, position, source, actionData);
    }

    @Override
    public IActionStatus doAction() {
        if (this.getWorld().isClientSide()) {
            return ActionResponses.COMPLETED;
        }

        // TODO spawn cluster body parts and fragments

        boolean spawnedSomething = false;

        final float yawAmount = 360.0f / projectilesPerLayer;

        int bombsToFire = this.spawnList.size();
        int discIndex = 0;
        while (bombsToFire > 0) {

            // Generate outwards disc
            for (int bombIndex = 0; bombIndex < this.projectilesPerLayer && bombsToFire > 0; bombIndex++) {
                // Decrease count
                bombsToFire -= 1;

                // calculate yaw
                final double yaw = Mth.wrapDegrees(yawAmount * bombIndex + (discIndex * discYawOffset));
                final Vec3 offsetYaw = RotationHelper.rotateY(SOUTH_VEC, yaw);
                final Vec3 sourceVec = RotationHelper.rotateY(RotationHelper.rotateX(offsetYaw, sourcePitch - 90), sourceYaw);

                // Calculate back motion to have projectiles shoot at an angle rather than direct 90
                final Vec3 backVector = RotationHelper.rotateX(UP_VEC, motionPitchOffset);

                // set base motion
                double motionX = (sourceVec.x + backVector.x) * motionScale;
                double motionY = (sourceVec.y + backVector.y) * motionScale;
                double motionZ = (sourceVec.z + backVector.z) * motionScale;
                // Increase motion by layers to prevent each layer hitting the same target
                motionX += (sourceVec.x + backVector.x) * motionScaleLayer * discIndex;
                motionY += (sourceVec.y + backVector.y) * motionScaleLayer * discIndex;
                motionZ += (sourceVec.z + backVector.z) * motionScaleLayer * discIndex;

                // Randomize motion to create a less perfect pattern
                if (motionRandomScale > 0) {
                    motionX += (getWorld().random.nextFloat() - getWorld().random.nextFloat()) * motionScaleLayer;
                    motionY += (getWorld().random.nextFloat() - getWorld().random.nextFloat()) * motionScaleLayer;
                    motionZ += (getWorld().random.nextFloat() - getWorld().random.nextFloat()) * motionScaleLayer;
                }

                // set position to slightly next to missile body
                final Vec3 stackVector = RotationHelper.rotateY(RotationHelper.rotateX(SOUTH_VEC, sourcePitch - 180), sourceYaw); //offset from disc, if facing down this is y++
                double x = (sourceVec.x * offsetScale) + (stackVector.x * stackScale * discIndex);
                double y = (sourceVec.y * offsetScale) + (stackVector.y * stackScale * discIndex);
                double z = (sourceVec.z * offsetScale) + (stackVector.z * stackScale * discIndex);


                //TODO confirm we spawned at least 1
                spawnedSomething = spawnProjectile(spawnList.get(bombsToFire), x, y, z, motionX, motionY, motionZ) || spawnedSomething;
            }

            // Move to next layer
            discIndex += 1;
        }

        // Fire a spawn action for each bomblet
        return spawnedSomething ? ActionResponses.COMPLETED : ActionResponses.ENTITY_SPAWN_FAILED;
    }

    private boolean spawnProjectile(ItemStack stackToSpawn, double x, double y, double z, double mx, double my, double mz) {
        final Entity entity = ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.spawnProjectile(stackToSpawn, getWorld(), getPosition().x + x, getPosition().y+ y, getPosition().z + z,
            null, this.allowPickupItem,
            (newEntity) -> {
                newEntity.setPos(getPosition().x + x, getPosition().y+ y, getPosition().z + z);
                applyMotion(newEntity, mx, my, mz);
            });

        // Spawn item to prevent loss
        if(entity == null) {
            final ItemEntity entityItem = new ItemEntity(EntityType.ITEM, getWorld());
            entityItem.setPos(getPosition().x + x, getPosition().y+ y, getPosition().z + z);
            entityItem.setItem(stackToSpawn.copy());
            entityItem.setDefaultPickUpDelay();
            applyMotion(entityItem, mx, my, mz);

            return getWorld().addFreshEntity(entityItem);
        }
        return true;
    }

    private static void applyMotion(Entity entity, double mx, double my, double mz) {
        entity.setDeltaMovement(mx, my, mz);

        // set rotation to match motion
        final float f3 = (float) Math.sqrt(mx * mx + mz * mz);
        final float yaw = (float) (Math.atan2(mx, mz) * 180.0D / Math.PI);
        final float pitch = (float) (Math.atan2(my, f3) * 180.0D / Math.PI);
        entity.setYRot(yaw);
        entity.yRotO = yaw;
        entity.setXRot(pitch);
        entity.xRotO = pitch;
    }
}
// PORTED
