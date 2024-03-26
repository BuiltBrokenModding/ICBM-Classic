package icbm.classic.api.radio.messages;

import icbm.classic.api.radio.IRadioMessage;
import net.minecraft.world.phys.Vec3;

/**
 * Packet containing targetting information
 */
public interface ITargetMessage extends IRadioMessage {

    Vec3 getTarget();
}
