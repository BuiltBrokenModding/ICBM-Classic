package icbm.classic.api.reg.content;

import icbm.classic.api.data.EntityTickFunction;
import icbm.classic.api.data.LevelEntityIntSupplier;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Created by Dark(DarkGuardsman, Robert) on 9/7/2019.
 */
public interface IExFuseEntityRegistry {
    /**
     * Called to set a supplier that will be used to define the fuse time
     * of the explosive.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName   - explosive to register with
     * @param supplier - function that will be called each tick
     */
    void setFuseSupplier(ResourceLocation exName, LevelEntityIntSupplier supplier);

    /**
     * Called to set a function to invoke each tick of an explosive block's fuse.
     * Use this to create interesting effects for unique explosives
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName   - explosive to register with
     * @param function - function that will be called each tick
     */
    void setFuseTickListener(ResourceLocation exName, EntityTickFunction function);

    /**
     * Called by objects to tick the fuse for the explosive
     *
     * @param entity       - entity that is the explosive
     * @param type         - explosive id
     * @param fuseTimeLeft - ticks existed
     */
    void tickFuse(Entity entity, ExplosiveType type, int fuseTimeLeft);

    /**
     * Gets the starting fuse time
     *
     * @param entity - entity that is the explosive
     * @param data   - explosive id
     * @return ticks
     */
    int getFuseTime(Entity entity, ExplosiveType data);
}
