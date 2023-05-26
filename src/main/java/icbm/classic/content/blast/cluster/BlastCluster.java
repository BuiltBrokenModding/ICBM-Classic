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
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class BlastCluster extends BlastBase {

    @Getter @Setter
    private float ejectionScale = 0.3f;

    /** Number of droplets to spawn */
    @Getter @Setter
    private int count = 100;

    /** Number of droplets per ejection disc */
    @Getter @Setter
    private int discSize = 10;

    /** Initial heading of the blast */
    @Getter @Setter
    private float yaw = 0;

    /** Initial heading of the blast */
    @Getter @Setter
    private float pitch = 0;

    /** Initial motion of the blast */
    private double motionX, motionY, motionZ;

    @Override
    public IBlastInit setBlastSource(Entity entity)
    {
        this.yaw = entity.rotationYaw;
        this.pitch = entity.rotationPitch;
        this.motionX = entity.motionX;
        this.motionY = entity.motionY;
        this.motionZ = entity.motionZ;
        return this;
    }
    @Override
    protected BlastResponse triggerBlast() {
        if (!world().isRemote)
        {
            final EulerAngle startingAngles = new EulerAngle(yaw, pitch);

            // Convert motion into a vector so we can offset position on spawn
            final double velocity = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            final double mx = motionX / velocity;
            final double my = motionY / velocity;
            final double mz = motionZ / velocity;

            final float yawAmount = 360.0f / discSize;
            int bombsToFire = this.count;
            int discIndex = 0;
            while(bombsToFire > 0) {

                // Generate outwards disc
                for (int bombIndex = 0; bombIndex < this.discSize && bombsToFire > 0; bombIndex++)
                {
                    // Decrease count
                    bombsToFire -= 1;

                    //TODO convert to factory
                    final EntityBombDroplet bomblet = new EntityBombDroplet(world());
                    bomblet.explosive.setStack(new ItemStack(ItemReg.itemBomblet));

                    final EulerAngle angle = new EulerAngle((yawAmount * bombIndex) + (discIndex % 2 == 1 ? 90 : 0), 110).add(startingAngles);
                    bomblet.initAimingPosition(
                        x() + (-mx * 0.5 * discIndex),
                        y() + (-my * 0.5 * discIndex),
                        z() + (-mz * 0.5 * discIndex),
                        (float) angle.yaw(),
                        (float) angle.pitch(),
                        0.3f, 0.3f + discIndex * 0.1f);

                    world().spawnEntity(bomblet); //TODO confirm we spawned at least 1
                }

                // Move to next layer
                discIndex += 1;
            }
        }
        return BlastState.TRIGGERED.genericResponse;
    }
}
