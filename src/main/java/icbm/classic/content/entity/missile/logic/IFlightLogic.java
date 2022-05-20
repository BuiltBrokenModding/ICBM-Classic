package icbm.classic.content.entity.missile.logic;

import icbm.classic.api.missiles.IMissileTarget;

/**
 * Handles motion update logic for missiles
 * <p>
 * Goal of this is to act much like a flight computer. It is not meant to be
 * a targeting system or smart AI guidance system. All logic should be simplistic
 * and built on basic rules. More advanced logic should instead interact with
 * the flight system to help guide it through updates to target or variable data.
 * <p>
 * Created by Robin Seifert on 2/7/2022.
 */
public interface IFlightLogic<TargetData extends IMissileTarget>
{
    /**
     * Called to set variables needed to run the flight logic
     */
    void initializeFlight(TargetData targetData);

    default void onEntityTick()
    {
    }

    /**
     * Callback to allow taking control of gravity and friction
     *
     * @return true to allow normal friction and gravity to apply
     */
    default boolean decreaseMotion()
    {
        return true;
    }

    /**
     * Called to update the missile's velocity vector
     *
     * @param updateFunc - callback to update motion
     */
    default void updateVector(final MotionUpdateFunc updateFunc)
    {
    }

    /**
     * Called to get the position over several ticks. Do not use this
     * to calculate position for anti-missile systems. Assume the anti-missile system
     * doesn't understand the exact flight logic and only understands motion vector.
     * This way we get a fair gameplay mechanic of anti-missile systems missing.
     *
     * @param builder - function to build position, {@link net.minecraft.util.math.Vec3d#Vec3d(double, double, double)}
     * @param <V>     - return type of the builder
     * @return return of the builder function
     */
    <V> V predictPosition(final VecBuilderFunc<V> builder, final int ticks);

    @FunctionalInterface
    interface MotionUpdateFunc
    {
        void apply(double x, double y, double z);
    }

    @FunctionalInterface
    interface VecBuilderFunc<V>
    {
        V apply(double x, double y, double z);
    }
}
