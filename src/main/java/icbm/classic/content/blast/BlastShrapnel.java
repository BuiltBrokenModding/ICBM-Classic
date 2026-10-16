package icbm.classic.content.blast;

import icbm.classic.content.entity.EntityFragments;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class BlastShrapnel extends Blast
{
    @Setter @Accessors(chain = true)
    private Function<Level, EntityFragments> projectile;

    @Override
    public boolean doExplode(int callCount)
    {
        if (!world().isClientSide())
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
                    final EntityFragments fragment = projectile.apply(world);

                    float rotationPitch = 0.0F + rotationStep * pitchIndex;
                    fragment.moveTo(location.x(), Math.floor(location.y()) + 1.5, location.z(), rotationYaw, rotationPitch); //TODO fix y-pos to not offset by 1.5
                    fragment.setPos(
                        fragment.getX() - (Mth.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F),
                        fragment.getY() - 0.10000000149011612D, //TODO figure out why magic number
                        fragment.getZ() - (Mth.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F));

                    final double motionX = (-Mth.sin(rotationYaw / 180.0F * (float) Math.PI) * Mth.cos(rotationPitch / 180.0F * (float) Math.PI));
                    final double motionZ = (Mth.cos(rotationYaw / 180.0F * (float) Math.PI) * Mth.cos(rotationPitch / 180.0F * (float) Math.PI));
                    final double motionY = (-Mth.sin(rotationPitch / 180.0F * (float) Math.PI));
                    fragment.setDeltaMovement(motionX, motionY, motionZ);

                    fragment.setArrowHeading(motionX * world().random.nextFloat(), motionY * world().random.nextFloat(), motionZ * world().random.nextFloat(), 0.5f + (0.7f * world().random.nextFloat()), 1.0F);
                    world().addFreshEntity(fragment);

                }
            }
        }
        // PORT: one-shot blast, return true so completeBlast runs and the blast leaves WorkTickingActionHandler.activeBlasts (1.12 left it there until world unload)
        return true;
    }
}
// PORTED
