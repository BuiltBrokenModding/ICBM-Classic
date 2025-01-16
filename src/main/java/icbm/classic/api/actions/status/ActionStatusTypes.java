package icbm.classic.api.actions.status;

import icbm.classic.api.data.meta.MetaTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**<pre>
 * List of types to better describe actions. Different conditions can be combined to create outcomes downstream.
 *
 * Types are color coded for easy usage downstream. {@link #RED} means no, {@link #YELLOW} & {@link #GREEN} are yes
 *
 * {@link #RED} is not meant to be a stop condition for a machine. It is meant to say something is likely wrong and
 * a user should be given pause. Machines may implement this as the user needing to bypass safety or confirm action.
 *
 * {@link #BLOCKING} is the actual stop condition that prevent logic from continuing. When this is provided in a
 * status then the user, process, or anything shouldn't continue. Most errors often combine this to prevent user
 * from making mistakes or when code can't continue due to missing data.
 *
 * {@link #YELLOW} is a softer version of {@link #RED} that should be considered as yes continue. Think of traffic
 * lights that indicate continue slowly.
 *
 * {@link #GREEN} is directly equal to yes in any boolean check. It may come in different flavors to better
 * describe actions and how to respond. However, all sub-types should be viewed as 'everything is good'
 *
 * Examples:
 *  {@link #ERROR} + {@link #READY} -> machine is in an error state but can still be forced to continue
 *  {@link #CAUTION} + {@link #READY} -> machine has a soft error that can be ignored and will likely work fine.
 *</pre>
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ActionStatusTypes {

    /** Root for all actions */
    public static final MetaTag ROOT = MetaTag.getOrCreateRoot("icbmclassic", "action.status");

    /** Status that prevents interaction from continuing */
    public static final MetaTag BLOCKING = MetaTag.getOrCreateSubTag(ROOT, "blocking");

    //<editor-folding description="Error/Stop Conditions">

    /** Root of all bad, error, stopping, or no answer conditions */
    public static final MetaTag RED = MetaTag.getOrCreateSubTag(ROOT, "red");

    /** Status that is providing an error */
    public static final MetaTag ERROR = MetaTag.getOrCreateSubTag(RED, "error");

    /** Error caused by user failed to enter data or not completing setup */
    public static final MetaTag ERROR_USER = MetaTag.getOrCreateSubTag(ERROR, "user");

    /** Error relating to code or something outside the user's control */
    public static final MetaTag ERROR_DEV = MetaTag.getOrCreateSubTag(ERROR, "dev");

    //</editor-folding>

    //<editor-folding description="Caution Conditions">

    /** Root of all warning, caution, or soft error conditions */
    public static final MetaTag YELLOW = MetaTag.getOrCreateSubTag(ROOT, "yellow");

    /** Status that is providing a warning */
    public static final MetaTag CAUTION = MetaTag.getOrCreateSubTag(YELLOW, "caution");

    /** Status note it is considered running some task (timer, charging, recipe, cooking, etc) */
    public static final MetaTag WAITING = MetaTag.getOrCreateSubTag(YELLOW, "waiting");

    //</editor-folding>

    //<editor-folding description="Good conditions">

    /** Root of all conditions that may be perceived as good, positive, continue, or yes condition */
    public static final MetaTag GREEN = MetaTag.getOrCreateSubTag(ROOT, "green");

    /** Status note it is considered a ready and waiting to start */
    public static final MetaTag READY = MetaTag.getOrCreateSubTag(GREEN, "ready");

    /** Status note it is considered done with it's task */
    public static final MetaTag DONE = MetaTag.getOrCreateSubTag(GREEN, "done");

    //</editor-folding>
}
