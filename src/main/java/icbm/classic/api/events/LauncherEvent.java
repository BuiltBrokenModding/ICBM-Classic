package icbm.classic.api.events;

import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.caps.IMissileLauncher;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public abstract class LauncherEvent extends Event
{
    public final IMissileLauncher launcher;
    public final IMissileHolder holder;

    public LauncherEvent(IMissileLauncher launcher, IMissileHolder holder)
    {
        this.launcher = launcher;
        this.holder = holder;
    }

    /**
     * Called before a missile is created and launched
     * towards its target. Use this to cancel the launch
     * or change data.
     * <p>
     * Called before {@link MissileEvent.PostLaunch}
     */
    @Cancelable
    public static class PreLaunch extends LauncherEvent
    {
        public PreLaunch(IMissileLauncher launcher, IMissileHolder holder)
        {
            super(launcher, holder);
        }
    }
}
