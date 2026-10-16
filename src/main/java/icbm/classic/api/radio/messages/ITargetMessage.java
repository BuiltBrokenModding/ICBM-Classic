package icbm.classic.api.radio.messages;

import icbm.classic.api.radio.IRadioMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Packet containing targeting information
 */
public interface ITargetMessage extends IRadioMessage {

    /**
     * Attempts to calculate an intercept. Not all
     * systems provide this information and will return
     * null if solution is not possible.
     *
     * @param x of shooter
     * @param y of shooter
     * @param z of shooter
     * @param velocity of interceptor
     *
     * @return position of target for intercept
     */
    @Nullable
    default Vec3 getIntercept(double x, double y, double z, double velocity) {
        return getTarget();
    }

    /**
     * Target position, may be pull from other
     * data so cache when possible.
     *
     * Will only return null if target was lost
     *
     * @return position of target
     */
    @Nullable
    Vec3 getTarget();

    /**
     * Possible entity target, not all target messages
     * will contain the entity.
     *
     * @return entity being targeted
     */
    @Nullable
    default Entity getTargetEntity() {
        return null;
    }
}
// PORTED
