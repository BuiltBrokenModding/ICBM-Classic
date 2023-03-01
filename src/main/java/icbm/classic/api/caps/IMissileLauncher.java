package icbm.classic.api.caps;

import icbm.classic.api.missiles.LaunchStatus;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Capability for accessing data about a launcher
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/9/19.
 */
public interface IMissileLauncher
{

    /**
     * Tries to launch the missile
     *
     * @param target to load into missile
     * @param cause to note, optional
     * @param simulate to do pre-flight checks and get current status
     * @return status of launch
     */
    LaunchStatus launch(Vec3d target, @Nullable Entity cause, boolean simulate); //TODO add object for trigger reason to wrapper more data
}
