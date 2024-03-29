package icbm.classic.api.radio.messages;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.radio.IRadioMessage;

/**
 * Packet containing incoming missile information from radar detection
 */
public interface IIncomingMissileMessage extends IRadioMessage, ITargetMessage {

    IMissile getMissile();
}
