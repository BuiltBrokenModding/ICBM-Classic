package icbm.classic.api.radio.messages;

import icbm.classic.api.radio.IRadioMessage;
import net.minecraft.util.math.Vec3d;

/**
 * Packet containing targetting information
 */
public interface ITargetMessage extends IRadioMessage {

    Vec3d getTarget();
}
