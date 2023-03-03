package icbm.classic.content.blocks.radarstation;

import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.lib.radio.imp.RadioTile;

public class RadioRadar extends RadioTile<TileRadarStation> implements IRadioSender {

    public RadioRadar(TileRadarStation host) {
        super(host);
    }

    @Override
    public void onMessageCallback(IRadioReceiver receiver, IRadioMessage response) {

    }
}
