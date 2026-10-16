package icbm.classic.api.radio.messages;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.radio.IRadioMessage;
import net.minecraft.world.entity.Entity;

/**
 * Packet containing incoming missile information from radar detection
 */
public interface IIncomingMissileMessage extends IRadioMessage, ITargetMessage, ITriggerActionMessage {

    IMissile getMissile();

    @Override
    default Entity getTargetEntity() {
        return getMissile().getMissileEntity();
    }
}
// PORTED
