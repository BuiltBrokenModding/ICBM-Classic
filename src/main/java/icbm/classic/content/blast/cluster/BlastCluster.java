package icbm.classic.content.blast.cluster;

import com.builtbroken.jlib.data.vector.Pos3D;
import com.builtbroken.jlib.data.vector.Pos3DBean;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.blast.imp.BlastBase;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.transform.RotationHelper;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlastCluster extends BlastBase {

    private static final Vec3d DEFAULT_FACE = new Vec3d(0, 0, 1);

    @Getter
    @Setter
    private float ejectionScale = 0.3f;

    /**
     * Number of droplets to spawn
     */
    @Getter
    @Setter
    private int count = 100;

    /**
     * Number of droplets per ejection disc
     */
    @Getter
    @Setter
    private int discSize = 10;

    /**
     * Initial heading of the blast
     */
    @Getter
    @Setter
    private float yaw = 0;

    /**
     * Initial heading of the blast
     */
    @Getter
    @Setter
    private float pitch = 0;

    /**
     * Initial motion of the blast
     */
    private double motionX, motionY, motionZ;

    @Override
    public IBlastInit setBlastSource(Entity entity) {
        this.yaw = entity.rotationYaw;
        this.pitch = entity.rotationPitch;
        this.motionX = entity.motionX;
        this.motionY = entity.motionY;
        this.motionZ = entity.motionZ;
        return this;
    }

    @Override
    protected BlastResponse triggerBlast() {
        if (!world().isRemote) {

            EntityBombDroplet bomblet = new EntityBombDroplet(world());
            bomblet.explosive.setStack(new ItemStack(ItemReg.itemBomblet));
            bomblet.setPosition(x(), y(), z());
            bomblet.initAimingPosition(
                x(),
                y(),
                z(),
                (float) yaw,
                (float) pitch,
                0f, 0.03f);
            world().spawnEntity(bomblet);

            final float yawAmount = 360.0f / discSize;

            // 90 is facing down and 0 is facing south
            final float offsetPitch = pitch - 90; // we want to be 90 above default
            final float motionPitch = offsetPitch;  // want to fire backwards to improve spread
            final float motionScale = 0.3f;
            final float motionScaleLayer = 0.1f;
            final float stackScale = 0.1f;
            final float offsetScale = 0.25f;

            int bombsToFire = this.count;
            int discIndex = 0;
            while (bombsToFire > 0) {

                // Generate outwards disc
                for (int bombIndex = 0; bombIndex < this.discSize && bombsToFire > 0; bombIndex++) {
                    // Decrease count
                    bombsToFire -= 1;

                    //TODO convert to factory
                    bomblet = new EntityBombDroplet(world());
                    bomblet.explosive.setStack(new ItemStack(ItemReg.itemBomblet));

                    // calculate yaw TODO consider inverting loop so we spawn a row, as that should be less calculations for rotation
                    final double yaw = MathHelper.wrapDegrees(yawAmount * bombIndex + (discIndex % 2 == 1 ? 90 : 0));
                    final Vec3d offsetYaw = RotationHelper.rotateY(DEFAULT_FACE, yaw);

                    // set motion to be away from missile body
                    final Vec3d moveVector = RotationHelper.rotateX(offsetYaw, motionPitch);
                    bomblet.motionX = moveVector.x * motionScale + moveVector.x * motionScaleLayer * discIndex;
                    bomblet.motionY = moveVector.y * motionScale + moveVector.y * motionScaleLayer * discIndex;
                    bomblet.motionZ = moveVector.z * motionScale + moveVector.z * motionScaleLayer * discIndex;

                    // set position to slightly next to missile body
                    final Vec3d offsetVector = RotationHelper.rotateX(offsetYaw, offsetPitch); //offset for ejection
                    final Vec3d stackVector = RotationHelper.rotateX(DEFAULT_FACE, pitch - 180); //offset from disc, if facing down this is y++
                    bomblet.setPosition(
                        x() + (offsetVector.x * offsetScale) + (stackVector.x * stackScale * discIndex),
                        y() + (offsetVector.y * offsetScale) + (stackVector.y * stackScale * discIndex),
                        z() + (offsetVector.z * offsetScale) + (stackVector.z * stackScale * discIndex));

                    // set rotation to match motion
                    final float f3 = MathHelper.sqrt(bomblet.motionX * bomblet.motionX + bomblet.motionZ * bomblet.motionZ);
                    bomblet.prevRotationYaw = bomblet.rotationYaw = (float) (Math.atan2(bomblet.motionX, bomblet.motionZ) * 180.0D / Math.PI);
                    bomblet.prevRotationPitch = bomblet.rotationPitch = (float) (Math.atan2(bomblet.motionY, f3) * 180.0D / Math.PI);

                    world().spawnEntity(bomblet); //TODO confirm we spawned at least 1
                }

                // Move to next layer
                discIndex += 1;
            }
        }
        return BlastState.TRIGGERED.genericResponse;
    }


}
