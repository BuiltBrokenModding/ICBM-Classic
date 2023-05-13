package icbm.classic.content.blocks.emptower;

import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.api.radio.messages.ITriggerActionMessage;
import icbm.classic.lib.radio.imp.RadioTile;
import icbm.classic.lib.radio.messages.TextMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class RadioEmpTower extends RadioTile<TileEMPTower> implements IRadioReceiver, INBTSerializable<NBTTagCompound> {

    public static final String SUCCESS = "tile.emptower.radio.success";

    public RadioEmpTower(TileEMPTower host) {
        super(host);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        if (canReceive(sender, packet)) {

            // Fire emp tower
            if(packet instanceof ITriggerActionMessage) {
                if(host.fire()) {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), SUCCESS));
                }
            }
        }
    }
}
