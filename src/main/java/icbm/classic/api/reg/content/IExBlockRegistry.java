package icbm.classic.api.reg.content;

import icbm.classic.api.EnumExplosiveType;
import icbm.classic.api.data.BlockActivateFunction;
import icbm.classic.api.data.WorldTypePosIntSupplier;
import icbm.classic.api.data.WorldTickFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExBlockRegistry extends IExplosiveContentRegistry
{

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
    void setFuseSupplier(ResourceLocation exName, WorldTypePosIntSupplier fuseTimer);

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
    void setFuseTickListener(ResourceLocation exName, WorldTickFunction function);

    /**
     * Called to set a function to invoke when an explosive block is clicked
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName
     * @param function
     */
    void setActivationListener(ResourceLocation exName, BlockActivateFunction function);

    /**
     * Called by objects to tick the fuse for the explosive
     *
     * @param world
     * @param posX
     * @param posY
     * @param posZ
     * @param ticksExisted
     */
    void tickFuse(World world, double posX, double posY, double posZ, int ticksExisted, int explosiveID);

    int getFuseTime(World world, EnumExplosiveType type, double posX, double posY, double posZ, int explosiveID);
}
