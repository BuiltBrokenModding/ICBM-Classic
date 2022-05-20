package icbm.classic.content.entity.missile.logic;

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
public class DirectFlightLogic implements IMissileFlightLogic
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "direct");
    private double deltaPathX, deltaPathY, deltaPathZ;

    private boolean hasRunSetup = false;
    double motionX;
    double motionY;
    double motionZ;

    @Override
    public void calculateFlightPath(World world, double startX, double startY, double startZ, IMissileTarget targetData)
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
        motionX *= ConfigMissile.LAUNCH_SPEED;
        motionY *= ConfigMissile.LAUNCH_SPEED;
        motionZ *= ConfigMissile.LAUNCH_SPEED;
    }

    @Override
    public void start(Entity entity)
    {
        if (!hasRunSetup)
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
    }

    //TODO rework to use current pitch and yaw to set motion
    //TODO update motion as long as we have fuel (ticks of motion time)

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        return builder.apply(
            entity.posX + entity.motionX * ticks, //TODO add gravity
            entity.posY + entity.motionY * ticks,
            entity.posZ + entity.motionZ * ticks
        );
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        return entity.ticksExisted > 1000; //TODO add config for fuel timer and adjust based on handheld vs cruise launcher
    }
}
