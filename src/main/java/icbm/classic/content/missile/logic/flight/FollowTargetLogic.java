package icbm.classic.content.missile.logic.flight;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.config.ConfigAntiMissile;
import icbm.classic.config.ConfigMissile;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Follows the location of the target data
 *
 * Created by Robin Seifert on 10/8/2022.
 */
public class FollowTargetLogic extends DeadFlightLogic
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "direct.follow");

    public FollowTargetLogic() {
        //For save/load
    }

    public FollowTargetLogic(int fuel) {
        super(fuel);
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir)
    {
        if(missile.getTargetData() != null) //TODO if we have no target data have missile fly around in random directions until it hits something
        {
            double motionX = missile.getTargetData().getX() - entity.posX;
            double motionY = missile.getTargetData().getY() - entity.posY;
            double motionZ = missile.getTargetData().getZ() - entity.posZ;

            float velocity = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= velocity;
            motionY /= velocity;
            motionZ /= velocity;

            //Set motion
            entity.motionX = motionX * ConfigAntiMissile.FLIGHT_SPEED;
            entity.motionY = motionY * ConfigAntiMissile.FLIGHT_SPEED;
            entity.motionZ = motionZ * ConfigAntiMissile.FLIGHT_SPEED;

            //Update rotation
            float f3 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            entity.prevRotationYaw = entity.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
            entity.prevRotationPitch = entity.rotationPitch = (float) (Math.atan2(motionY, (double) f3) * 180.0D / Math.PI);
        }
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }
}
