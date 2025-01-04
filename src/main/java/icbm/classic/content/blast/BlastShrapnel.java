package icbm.classic.content.blast;

import icbm.classic.content.entity.EntityFragments;
import icbm.classic.lib.projectile.EntityProjectile;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Function;

public class BlastShrapnel extends Blast
{
    @Setter @Accessors(chain = true)
    private Function<World, Entity> projectile;

    @Override
    public boolean doExplode(int callCount)
    {
        if (!world().isRemote)
        {
            float rotationStep = 360 / this.getBlastRadius();

            // TODO add logic to detect when rotation is blocked by ground and to avoid spawning fragments
            for (int yawIndex = 0; yawIndex < this.getBlastRadius(); yawIndex++)
            {
                // Try to do a 360 explosion on all 6 faces of the cube.
                float rotationYaw = 0.0F + rotationStep * yawIndex;

                // TODO randomize position, velocity y rotation to create a more realistic blast fragmentation effect
                for (int pitchIndex = 0; pitchIndex < this.getBlastRadius(); pitchIndex++)
                {
                    final Entity fragment = projectile.apply(world);

                    float rotationPitch = 0.0F + rotationStep * pitchIndex;
                    fragment.setLocationAndAngles(location.x(), Math.floor(location.y()) + 1.5, location.z(), rotationYaw, rotationPitch); //TODO fix y-pos to not offset by 1.5
                    fragment.posX -= (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    fragment.posY -= 0.10000000149011612D; //TODO figure out why magic number
                    fragment.posZ -= (MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    fragment.setPosition(fragment.posX, fragment.posY, fragment.posZ);

                    fragment.motionX = (-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
                    fragment.motionZ = (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
                    fragment.motionY = (-MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI));

                    fragment.setArrowHeading(fragment.motionX * world().rand.nextFloat(), fragment.motionY * world().rand.nextFloat(), fragment.motionZ * world().rand.nextFloat(), 0.5f + (0.7f * world().rand.nextFloat()), 1.0F);
                    world().addEntity(fragment);

                }
            }
        }
        return false;
    }
}
