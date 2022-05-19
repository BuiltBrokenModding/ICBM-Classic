package icbm.classic.api.missiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Abstracted targeting logic for missiles
 */
public interface IMissileTarget extends INBTSerializable<NBTTagCompound> {

    /**
     * Target position, assume this is not cached and can change
     *
     * @return current targeting position
     */
    Vec3d getPosition();

    /**
     * Checks if the target data is valid
     */
    boolean isValid();

    /**
     * Getter for x position, assume this is not cached and can change
     * @return double or NaN if no target is set
     */
    double getX();

    /**
     * Getter for y position, assume this is not cached and can change
     * @return double or NaN if no target is set
     */
    double getY();

    /**
     * Getter for z position, assume this is not cached and can change
     * @return double or NaN if no target is set
     */
    double getZ();
}
