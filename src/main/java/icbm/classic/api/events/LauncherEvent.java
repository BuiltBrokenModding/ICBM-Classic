package icbm.classic.api.events;

import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.parts.IMissileTarget;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public abstract class LauncherEvent extends Event
{
    /** Starting point of the missile */
    public final IMissileSource source;
    /** Launcher capability, can be used to relaunch missile with edits */
    public final IMissileLauncher launcher;
    /** Missile inventory */
    public final IMissileHolder holder;

    public LauncherEvent(IMissileSource source, IMissileLauncher launcher, IMissileHolder holder)
    {
        this.source = source;
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
        /** Target of launcher, doesn't account for offsets or inaccuracy */
        public final IMissileTarget target;

        /** True if launch was simulated */
        public final boolean simulate;

        /** Optional reason for event being canceled, defaults to 'launcher.status.icbmclassic:message.canceled' */
        public IActionStatus cancelReason;

        public PreLaunch(IMissileSource source, IMissileLauncher launcher, IMissileHolder holder, IMissileTarget target, boolean simulate)
        {
            super(source, launcher, holder);
            this.target = target;
            this.simulate = simulate;
        }
    }
}
