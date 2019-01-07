package icbm.classic.api.reg.content;

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
    void addLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    /**
     * Adds a simple callback for when a missile updates.
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific missile types.
     *
     * @param exName        - id of the explosive/missile
     * @param eventCallback - function to call
     */
    void addMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback);

    //TODO add handling for insert into launcher (cruise launcher prevents cluster and homing)
}
