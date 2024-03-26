package icbm.classic.api.explosion;

import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Separate interface for blasts that can be built from {@link IBlastFactory}
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public interface IBlastInit extends IBlast {

    /**
     * Sets the size of the blast
     *
     * @param size - size of the blast, this differs base on explosive of how it is used.
     * @return this
     */
    default IBlastInit setBlastSize(double size) {
        return this;
    }

    /**
     * Scales the size of the blast
     *
     * @param scale - multiplier to apply
     * @return this
     */
    default IBlastInit scaleBlast(double scale) {
        return this;
    }

    /**
     * Called to set the source of the blast. Only
     * call on init of the blast.
     *
     * @param entity - source of the blast
     * @return this
     */
    default IBlastInit setBlastSource(Entity entity) { //TODO maybe consider using a blame object that wrappers the source in case it dies
        return this;
    }

    /**
     * Called to set the world of the blast. Only
     * call on init of the blast. The world should never
     * change for a blast. If you teleport the blast through
     * a portal destroy and recreate to kill connections.
     *
     * @param level
     * @return this
     */
    IBlastInit setBlastLevel(Level level);

    /**
     * Sets the blast's position. Should only be called
     * on init of the blast. Use separate methods to move
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
    default IBlastInit setCustomData(@Nonnull CompoundTag customData) {
        return this;
    }

    /**
     * Sets the entity that will control this blast.
     * <p>
     * This is called after {@link #buildBlast()} so do
     * not lock after the blast is built. As controller
     * may need to refresh or change dimensions.
     *
     * @param entityController
     * @return this
     */
    default IBlastInit setEntityController(Entity entityController) {
        return this;
    }

    /**
     * Sets the explosive data used to create this blast
     *
     * @param data
     * @return this
     */
    default IBlastInit setExplosiveData(ExplosiveType data) {
        return this;
    }

    /**
     * Called last to complete the build of the blast. Once
     * called all methods in this interface should ignore
     * changes.
     *
     * @return this
     */
    IBlastInit buildBlast();
}
