package icbm.classic.content.blast;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Function;

public class BlastShrapnel extends Blast {
    @Setter
    @Accessors(chain = true)
    private Function<World, Entity> projectile;

    @Override
    public boolean doExplode(int callCount) {
        if (!getWorld().isRemote) {
            float rotationStep = 360 / this.getBlastRadius();

            // TODO add logic to detect when rotation is blocked by ground and to avoid spawning fragments
            for (int yawIndex = 0; yawIndex < this.getBlastRadius(); yawIndex++) {
                // Try to do a 360 explosion on all 6 faces of the cube.
                float rotationYaw = 0.0F + rotationStep * yawIndex;

                // TODO randomize position, velocity y rotation to create a more realistic blast fragmentation effect
                for (int pitchIndex = 0; pitchIndex < this.getBlastRadius(); pitchIndex++) {
                    final Entity fragment = projectile.apply(world);

                    float rotationPitch = 0.0F + rotationStep * pitchIndex;
                    fragment.setLocationAndAngles(getPosition().x, Math.floor(getPosition().y) + 1.5, getPosition().z, rotationYaw, rotationPitch); //TODO fix y-pos to not offset by 1.5
                    fragment.posX -= (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    fragment.posY -= 0.10000000149011612D; //TODO figure out why magic number
                    fragment.posZ -= (MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    fragment.setPosition(fragment.posX, fragment.posY, fragment.posZ);

                    fragment.setMotion(
                        -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI),
                        -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI),
                        MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI)
                    );

                    if (fragment instanceof IProjectile) {
                        ((IProjectile) fragment).shoot(
                            fragment.getMotion().x * getWorld().rand.nextFloat(),
                            fragment.getMotion().y * getWorld().rand.nextFloat(),
                            fragment.getMotion().z * getWorld().rand.nextFloat(),
                            0.5f + (0.7f * getWorld().rand.nextFloat()), 1.0F);
                    }
                    getWorld().addEntity(fragment);

                }
            }
        }
        return false;
    }
}
