package icbm.classic.world.blast;

import icbm.classic.world.entity.FragmentsEntity;
import net.minecraft.util.math.MathHelper;

public class BlastShrapnel extends Blast {
    private boolean isExplosive = false;
    private boolean isAnvil = false;

    public BlastShrapnel() {
    }

    public BlastShrapnel setFlaming() {
        this.causesFire = true; //TODO convert to factory
        return this;
    }

    public BlastShrapnel setExplosive() {
        this.isExplosive = true; //TODO convert to factory
        return this;
    }

    public BlastShrapnel setAnvil() {
        this.isAnvil = true; //TODO convert to factory
        return this;
    }

    @Override
    public boolean doExplode(int callCount) {
        if (!level().isClientSide()) {
            float amountToRotate = 360 / this.getBlastRadius();

            for (int i = 0; i < this.getBlastRadius(); i++) {
                // Try to do a 360 explosion on all 6 faces of the cube.
                float rotationYaw = 0.0F + amountToRotate * i;

                for (int ii = 0; ii < this.getBlastRadius(); ii++) {
                    //TODO convert to factory
                    FragmentsEntity arrow = new FragmentsEntity(level(), location.x(), location.y() + 0.5, location.z(), this.isExplosive, this.isAnvil);

                    if (this.causesFire) {
                        arrow.arrowCritical = true;
                        arrow.setFire(100);
                    }

                    float rotationPitch = 0.0F + amountToRotate * ii;
                    arrow.setLocationAndAngles(location.x(), Math.floor(location.y()) + 1.5, location.z(), rotationYaw, rotationPitch);
                    arrow.getX() -= (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    arrow.getY() -= 0.10000000149011612D;
                    arrow.getZ() -= (MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
                    arrow.setPosition(arrow.getX(), arrow.getY(), arrow.getZ());
                    //arrow.yOffset = 0.0F;
                    arrow.motionX = (-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
                    arrow.motionZ = (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
                    arrow.motionY = (-MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI));
                    arrow.setArrowHeading(arrow.motionX * level().rand.nextFloat(), arrow.motionY * level().rand.nextFloat(), arrow.motionZ * level().rand.nextFloat(), 0.5f + (0.7f * level().rand.nextFloat()), 1.0F);
                    level().spawnEntity(arrow);

                }
            }
        }
        return false;
    }
}
