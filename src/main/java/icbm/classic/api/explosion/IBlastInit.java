package icbm.classic.api.explosion;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Separate interface for blasts that can be built from {@link IBlastFactory}
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public interface IBlastInit extends IBlast
{

    /**
     * Sets the size of the blast
     *
     * @param size - size of the blast, this differs base on explosive of how it is used.
     * @return this
     */
    IBlastInit setBlastSize(double size);


    /**
     * Called to set the source of the blast. Only
     * call on init of the blast.
     *
     * @param entity - source of the blast
     * @return this
     */
    IBlastInit setBlastSource(Entity entity); //TODO maybe consider using a blame object that wrappers the source in case it dies

    /**
     * Called to set the world of the blast. Only
     * call on init of the blast. The world should never
     * change for a blast. If you teleport the blast through
     * a portal destroy and recreate to kill connections.
     *
     * @param world
     * @return this
     */
    IBlastInit setBlastWorld(World world);

    /**
     * Sets the blast's position. Should only be called
     * on init of the blast. Use seperate methods to move
     * or teleport the blast.
     *
     * @param x
     * @param y
     * @param z
     * @return this
     */
    IBlastInit setBlastPosition(double x, double y, double z);

    /**
     * Sets the custom data to change properties of the blast
     * not part of the normal blast chain of calls.
     *
     * @param customData - nbt save data
     * @return this
     */
    IBlastInit setCustomData(@Nonnull NBTTagCompound customData);

    /**
     * Called last to complete the build of the blast. Once
     * called all methods in this interface should ignore
     * changes.
     *
     * @return this
     */
    IBlastInit buildBlast();
}
