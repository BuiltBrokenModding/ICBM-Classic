package icbm.classic.api.missiles;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
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
public interface IMissileFlightLogic
{
    /**
     * Called to set our path data based on start position and target
     *
     * Once call the implementation should lock and ignore additional calls.
     */
    void calculateFlightPath(final World world, final double startX, final double startY, final double startZ, final IMissileTarget targetData);

    /**
     * Called to start the flight logic
     *
     * @param entity running the logic
     */
    default void start(Entity entity) {

    }

    /**
     * Called each tick of the missile
     */
    default void onEntityTick(Entity entity, int ticksInAir)
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
     * Name used to register the builder for this type in {@link icbm.classic.api.reg.obj.IMissileFlightLogicReg}
     * @return registry name
     */
    ResourceLocation getRegistryName();

    /**
     * Save callback
     * @return save data, or null to save nothing
     */
    default NBTTagCompound save() {
        return null;
    }

    /**
     * Load callback
     * @param save data used
     */
    default void load(NBTTagCompound save) {

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
