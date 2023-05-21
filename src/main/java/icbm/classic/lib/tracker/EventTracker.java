package icbm.classic.lib.tracker;

import icbm.classic.ICBMClassic;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

/**
 * System designed to track events happening in the game. As a way to give access to read-only
 * information for purpose of debugging, logging, or other mechanics. This is not meant to replace
 * event-bus provide by forge ecosystem. In the majority of cases any addon or mod should use the event
 * bus to listen for interaction. As this system will not provide a way to access back into the game. All
 * data will be intentionally isolated and made read-only.
 *
 * One of the main purposes of this system is to track user interaction. This way addons/plugins can
 * watch what a player does. Allowing for logging out to a database either for admin systems
 * or other utilities. What is considered user interaction will vary heavily. As it can be direct
 * player driven actions such as clicks, movement, or triggers. Along with indirect actions caused by placing
 * blocks or triggering launchers. There may be a long chain between the player and the action. Including
 * large passages of time. Such as building a launcher and days later triggering via redstone.
 *
 * Other interaction outside of users will be tracked. Such as missile launches, failures in the mod, and debugger
 * data. For those use to tech world... can think of this like <a href=https://www.splunk.com/>Splunk</a>. Which
 * provides logging support on servers and software. Only this event system would be the input into Splunk. With splunk
 * actually being a very valid output for the system if someone makes an adapter.
 *
 * For the development side, this will be used for logging useful information. Such as what a missile was doing at
 * an exact method entry/exit. Why something didn't follow the happy path.. or just general error that users can ignore.
 * Traditionally this was handled with console logs or other text utilities. In which it could be difficult to
 * keep consistent or follow through with more complex information. Using an event system allows higher levels of
 * customization of logs and use of external tools. Such as visualization via a swing-ui of realtime data. As well
 * developer tools to see raw outputs without using breakpoints.
 *
 * A purpose this will never be used for is anonymous user tracking. As useful as this could
 * be for development... there is no easy way to do this ethically. Any data collection at
 * this scale needs to be done in a dedicated mod. With fully visibility to the user that
 * it is happening and mechanics to disable it. Something that should never exist inside
 * a mod itself.
 */
public class EventTracker {

    private final List<ListenerEntry> listeners = new ArrayList();

    /**
     * Adds a listener for events. Events can come from different threads. So
     * ensure that your logic is thread safe.
     *
     * @param listener to add
     */
    public EventTracker listen(ResourceLocation name, IEventTrackerListener listener) {
        listeners.add(new ListenerEntry(name, listener));
        return this;
    }


    /**
     * Posts an event if the condition is meet. Useful utility to one line debug information.
     *
     * @param type of event
     * @param condition to check, often see if we are in debug mode
     * @param data to supply
     */
    public void post(EventTrackerType type, Supplier<Boolean> condition, Supplier<Object[]> data) {
        if(condition.get()) {
            post(type, data);
        }
    }

    /**
     * Posts an event, will only build the event entry if listeners exist to consume it.
     *
     * @param type of event
     * @param supplier to invoke to create data
     */
    public void post(EventTrackerType type, Supplier<Object[]> supplier) {
        if(consumes(type)) {
            final Object[] data = supplier.get();
            if(isDataReadOnly(type, data)) {
                post(new EventTrackerEntry(type, data));
            }
        }
    }

    /**
     * Posts an event, normally you want {@link #post(EventTrackerType, Supplier)} unless
     * this is being called by an event builder.
     *
     * @param entry to post
     */
    public void post(EventTrackerEntry entry) {
        post(entry, this.listeners);
        post(entry, entry.getType().getListeners());
    }

    protected void post(EventTrackerEntry entry, List<ListenerEntry> listeners) {
       for(IEventTrackerListener listener : listeners) {
           if(listener.consumes(entry.getType())) {
               listener.accept(entry);
           }
       }
    }

    /**
     * Used to check we are not leaking editable objects
     *
     * @param data to validate
     * @return true if the data is read-only
     */
    protected final boolean isDataReadOnly(EventTrackerType type, Object[] data) {
        boolean good = true;
        for(Object object: data) {
            if(object != null && EventTrackerData.isValidType(object.getClass())) {
                good = false;
                ICBMClassic.logger().warn(String.format("EventTracker(%s): Class['%s'] is not supported for use with events.", type.getName(), object.getClass()));
            }
        }
        if(!good) {
            final String msg = String.format("EventTracker(%s): Ignoring event due to containing editable fields. Supplying said fields could result in listeners incorrectly editing the game state", type.getName());
            ICBMClassic.logger().error(msg, new RuntimeException("Dev Bug, likely caused by domain '" + type.getName().getResourceDomain() + "' but may be another mod using the domain incorrectly"));
        }
        return good;
    }



    /**
     * Checks if this tracker can consume the event type
     *
     * @param type to check
     * @return true if it can consume it
     */
    public boolean consumes(EventTrackerType type) {
        if(this.listeners.isEmpty() && type.getListeners().isEmpty()) {
            return false;
        }
        // Using a loop to prevent memory overhead from streams
        for(IEventTrackerListener listener : type.getListeners()) {
            if(listener.consumes(type)) {
                return true;
            }
        }
        for(IEventTrackerListener listener : listeners) {
            if(listener.consumes(type)) {
                return true;
            }
        }
        return false;
    }
}
