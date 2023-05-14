package icbm.classic.lib.network.lambda;

import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.lib.radio.imp.Radio;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class GeneralCodexs {

    public static <T extends TileEntity> PacketCodexTile<T, IRadioChannelAccess> radioChannel(ResourceLocation parent, Function<T, IRadioChannelAccess> converter) {
        return (PacketCodexTile<T, IRadioChannelAccess>) new PacketCodexTile<T, IRadioChannelAccess>(parent, "radio.frequency", converter)
            .fromClient()
            .nodeString(IRadioChannelAccess::getChannel, IRadioChannelAccess::setChannel)
            .onFinished((tile, target, player) -> tile.markDirty());
    }

    public static <T extends TileEntity> PacketCodexTile<T, Radio> radioToggleDisable(ResourceLocation parent, Function<T, Radio> converter) {
        return (PacketCodexTile<T, Radio>) new PacketCodexTile<T, Radio>(parent, "radio.disable.toggle", converter)
            .fromClient()
            .toggleBoolean(Radio::isDisabled, Radio::setDisabled)
            .onFinished((tile, target, player) -> tile.markDirty());
    }
}
