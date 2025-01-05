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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ActionCluster extends ActionBase {

    private static final Vec3d SOUTH_VEC = new Vec3d(0, 0, 1);
    private static final Vec3d UP_VEC = new Vec3d(0, 1, 0);
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

    public ActionCluster(World world, Vec3d position, IActionSource source, IActionData actionData) {
        super(world, position, source, actionData);
    }

    @Override
    public IActionStatus doAction() {
        if (this.getWorld().isRemote) {
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
                if (motionRandomScale > 0) {
                    motionX += (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * motionScaleLayer;
                    motionY += (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * motionScaleLayer;
                    motionZ += (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * motionScaleLayer;
                }

                // set position to slightly next to missile body
                final Vec3d stackVector = RotationHelper.rotateY(RotationHelper.rotateX(SOUTH_VEC, sourcePitch - 180), sourceYaw); //offset from disc, if facing down this is y++
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
                newEntity.setPosition(getPosition().x + x, getPosition().y+ y, getPosition().z + z);
                newEntity.setMotion(mx, my, mz);

                // set rotation to match motion
                final float f3 = MathHelper.sqrt(newEntity.getMotion().x * newEntity.getMotion().x + newEntity.getMotion().z * newEntity.getMotion().z);
                newEntity.prevRotationYaw = newEntity.rotationYaw = (float) (Math.atan2(newEntity.getMotion().x, newEntity.getMotion().z) * 180.0D / Math.PI);
                newEntity.prevRotationPitch = newEntity.rotationPitch = (float) (Math.atan2(newEntity.getMotion().y, f3) * 180.0D / Math.PI);
            });

        // Spawn item to prevent loss
        if(entity == null) {
            final ItemEntity entityItem = EntityType.ITEM.create(getWorld());
            entityItem.setPosition(getPosition().x + x, getPosition().y+ y, getPosition().z + z);
            entityItem.setItem(stackToSpawn.copy());
            entityItem.setDefaultPickupDelay();
            entityItem.setMotion(mx, my, mz);

            // set rotation to match motion
            final float f3 = MathHelper.sqrt(entityItem.getMotion().x * entityItem.getMotion().x + entityItem.getMotion().z * entityItem.getMotion().z);
            entityItem.prevRotationYaw = entityItem.rotationYaw = (float) (Math.atan2(entityItem.getMotion().x, entityItem.getMotion().z) * 180.0D / Math.PI);
            entityItem.prevRotationPitch = entityItem.rotationPitch = (float) (Math.atan2(entityItem.getMotion().y, f3) * 180.0D / Math.PI);

            return getWorld().addEntity(entityItem);
        }
        return true;
    }
}
