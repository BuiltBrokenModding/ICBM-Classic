package icbm.classic.api.launcher;

import icbm.classic.api.missiles.parts.IBuildableObject;
import net.minecraft.network.chat.Component;

/**
 * Status response for triggering an action. This can be used
 * for various events that were triggered.
 * <p>
 * Specific examples will be missile launchers and emp towers.
 * Both will respond with a status of what happened after they
 * were triggered by the player.
 */
public interface IActionStatus extends IBuildableObject {

    /**
     * Is the status an error state
     *
     * @return true if error
     */
    boolean isError();

    /**
     * Should interaction be blocked while
     * status is active.
     *
     * @return true to block interaction.
     */
    default boolean shouldBlockInteraction() {
        return isError();
    }

    /**
     * Localization for error output. Assume this
     * will be displayed to a user.
     *
     * @return message
     */
    Component message();

    // TODO add a status callback for when 'status=aiming` or 'status=delay'
}
