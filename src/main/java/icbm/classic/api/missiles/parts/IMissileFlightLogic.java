package icbm.classic.api.missiles.parts;

import icbm.classic.api.missiles.IMissile;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

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
public interface IMissileFlightLogic extends IMissilePart
{
    /**
     * Called to set our path data based on start position and target
     *
     * Once call the implementation should lock and ignore additional calls.
     */
    default void calculateFlightPath(final World world, final double startX, final double startY, final double startZ, final IMissileTarget targetData) {

    }

    /**
     * Called to start the flight logic
     *
     * @param entity running the logic
     */
    default void start(Entity entity) {

    }

    /**
     * Called each tick of the missile motion
     */
    default void onEntityTick(Entity entity, IMissile missile, int ticksInAir)
    {
    }

    /**
     * Callback to allow taking control of gravity and friction
     *
     * Do custom friction and gravity in update tick
     *
     * @return true to allow normal friction and gravity to apply
     */
    default boolean shouldDecreaseMotion(Entity entity)
    {
        return true;
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
    <V> V predictPosition(final Entity entity, final VecBuilderFunc<V> builder, final int ticks);

    /**
     * Call back to see if the engine effects should run
     * @return true to run default engine effects
     */
    default boolean shouldRunEngineEffects(Entity entity) {
        return true;
    }

    default float engineSmokeRed(Entity entity) {
        // TODO consider using an object for storing settings
        return 1;
    }

    default float engineSmokeGreen(Entity entity) {
        return 1;
    }

    default float engineSmokeBlue(Entity entity) {
        return 1;
    }

    /**
     * Checks to see if we can safely exit the flight logic without issues.
     *
     * This is called often before switching from one flight logic set to another. Specific
     * cases may be a missile is exiting a launcher and now wants to move to target updating
     * logic. Such as with the AB missiles that may fly towards a target.
     *
     * Safe usually means we have finished any startup movements and are at low risk
     * to impacting walls in a player's base. This doesn't mean we should check if it is
     * actually safe unless the flight logic is built for that purpose.
     *
     * @return true if logic is safe to exit
     */
    default boolean canSafelyExitLogic() {
        return true;
    }

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
