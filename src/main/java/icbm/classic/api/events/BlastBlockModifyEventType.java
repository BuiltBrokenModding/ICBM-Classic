package icbm.classic.api.events;

/**
 * An enumeration specifying the type of modification that is to be made when a BlastBlockModifyEvent is handled.
 *
 * Created by AFlyingCar on 5/5/20
 */
public enum BlastBlockModifyEventType {
    SET_TO_AIR,
    SET_STATE,
    SET_STATE_WITH_FLAGS,
    USE_CALLBACK
}
