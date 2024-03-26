package icbm.classic.world.missile.logic.flight;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.config.missile.ConfigMissile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;

/**
 * Follows the location of the target data
 * <p>
 * Created by Robin Seifert on 10/8/2022.
 */
public class FollowTargetLogic extends DeadFlightLogic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "direct.follow");

    public FollowTargetLogic() {
        //For save/load
    }

    public FollowTargetLogic(int fuel) {
        super(fuel);
    }

    @Override
    public float engineSmokeRed(Entity entity) {
        return 1;
    }

    @Override
    public float engineSmokeGreen(Entity entity) {
        return 0.5f;
    }

    @Override
    public float engineSmokeBlue(Entity entity) {
        return 0.5f;
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        super.onEntityTick(entity, missile, ticksInAir);
        if (!entity.world.isClientSide() && hasFuel(entity) && missile.getTargetData() != null) //TODO if we have no target data have missile fly around in random directions until it hits something
        {
            double motionX = missile.getTargetData().getX() - entity.getX();
            double motionY = missile.getTargetData().getY() - entity.getY();
            double motionZ = missile.getTargetData().getZ() - entity.getZ();

            float velocity = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= velocity;
            motionY /= velocity;
            motionZ /= velocity;

            //Set motion
            entity.motionX = motionX * ConfigMissile.SAM_MISSILE.FLIGHT_SPEED;
            entity.motionY = motionY * ConfigMissile.SAM_MISSILE.FLIGHT_SPEED;
            entity.motionZ = motionZ * ConfigMissile.SAM_MISSILE.FLIGHT_SPEED;

            //Update rotation
            float f3 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            entity.prevRotationYaw = entity.getYRot() = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
            entity.prevRotationPitch = entity.getXRot() = (float) (Math.atan2(motionY, (double) f3) * 180.0D / Math.PI);
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }
}
