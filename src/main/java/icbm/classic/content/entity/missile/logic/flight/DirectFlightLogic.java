package icbm.classic.content.entity.missile.logic.flight;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.config.ConfigMissile;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Created by Robin Seifert on 2/8/2022.
 */
public class DirectFlightLogic extends DeadFlightLogic
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "direct");
    double motionX;
    double motionY;
    double motionZ;

    @Override
    public void calculateFlightPath(World world, double startX, double startY, double startZ, IMissileTarget targetData)
    {
        if(targetData != null) //TODO if we have no target data have missile fly around in random directions until it hits something
        {
            motionX = targetData.getX() - startX;
            motionY = targetData.getY() - startY;
            motionZ = targetData.getZ() - startZ;

            //Normalize
            float velocity = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= velocity;
            motionY /= velocity;
            motionZ /= velocity;

            //Add multiplier
            motionX *= ConfigMissile.DIRECT_FLIGHT_SPEED;
            motionY *= ConfigMissile.DIRECT_FLIGHT_SPEED;
            motionZ *= ConfigMissile.DIRECT_FLIGHT_SPEED;
        }
    }

    @Override
    public void start(Entity entity)
    {
        //Set motion
        entity.motionX = motionX;
        entity.motionY = motionY;
        entity.motionZ = motionZ;

        //Update rotation
        float f3 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        entity.prevRotationYaw = entity.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        entity.prevRotationPitch = entity.rotationPitch = (float) (Math.atan2(motionY, (double) f3) * 180.0D / Math.PI);
    }

    //TODO rework to use current pitch and yaw to set motion
    //TODO update motion as long as we have fuel (ticks of motion time)

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }
}
