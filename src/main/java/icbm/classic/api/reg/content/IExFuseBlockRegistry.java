package icbm.classic.api.reg.content;

import icbm.classic.api.data.LevelPosIntSupplier;
import icbm.classic.api.data.LevelTickFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robert) on 9/7/2019.
 */
public interface IExFuseBlockRegistry {
    /**
     * Called to set a supplier that will be used to define the fuse time
     * of the explosive.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName
     * @param fuseTimer
     */
    void setFuseSupplier(ResourceLocation exName, LevelPosIntSupplier fuseTimer);

    /**
     * Called to set a function to invoke each tick of an explosive block's fuse.
     * Use this to create interesting effects for unique explosives
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName
     * @param function
     */
    void setFuseTickListener(ResourceLocation exName, LevelTickFunction function);

    /**
     * Called by objects to tick the fuse for the explosive
     *
     * @param world
     * @param posX
     * @param posY
     * @param posZ
     * @param ticksExisted
     */
    void tickFuse(Level level, double posX, double posY, double posZ, int ticksExisted, int explosiveID);

    int getFuseTime(Level level, double posX, double posY, double posZ, int explosiveID);
}
