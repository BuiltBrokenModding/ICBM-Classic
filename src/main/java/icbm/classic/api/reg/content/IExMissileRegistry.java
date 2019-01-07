package icbm.classic.api.reg.content;

import icbm.classic.api.data.EntityInteractionFunction;
import icbm.classic.api.explosion.ILauncherContainer;
import icbm.classic.api.explosion.IMissile;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExMissileRegistry extends IExplosiveContentRegistry
{

    /**
     * Adds a simple callback for when a missile launches
     * <p>
     * * Do not use this in place of normal events, this is designed to add logic for
     * * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param eventCallback - function to call
     */
    void setLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    /**
     * Adds a simple callback for when a missile updates.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param eventCallback - function to call
     */
    void setMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    /**
     * Adds a simple callback for player interaction with missile. This should be used
     * to encoded data into the missile. Such as setting teleportation coordinates in the
     * same way the ender blast does.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param function - function to call
     */
    void setInteractionListener(ResourceLocation exName, EntityInteractionFunction function);

    //TODO add handling for insert into launcher (cruise launcher prevents cluster and homing)
}
