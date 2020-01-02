package icbm.classic.api.caps;

import icbm.classic.api.data.LaunchStatus;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability for accessing data about a launcher
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public interface IMissileLauncher
{

    /**
     * Wrapper to the missile holder
     *
     * @return
     */
    @Nonnull
    IMissileHolder getMissileHolder();

    /**
     * Tries to launch the missile
     *
     * @param cause - entity that triggered the launch, can't always be accessed
     * @return status of launch
     */
    LaunchStatus launchMissile(@Nullable Entity cause); //TODO add object for trigger reason to wrapper more data

    /**
     * Status of the launcher
     *
     * @return
     */
    LaunchStatus getLauncherStatus();

    /**
     * Sets the target of the launcher
     *
     * @param x
     * @param y
     * @param z
     */
    void setTarget(double x, double y, double z);

    /**
     * Gets the target of the launcher as a block pos
     *
     * @return
     */
    @Nullable
    BlockPos getTarget();

    double getTargetX();

    double getTargetY();

    double getTargetZ();
}
