package icbm.classic.lib.network.lambda;

import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.radio.imp.Radio;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class GeneralCodexs {

    public static <T extends BlockEntity> PacketCodexTile<T, IRadioChannelAccess> radioChannel(ResourceLocation parent, Function<T, IRadioChannelAccess> converter) {
        return (PacketCodexTile<T, IRadioChannelAccess>) new PacketCodexTile<T, IRadioChannelAccess>(parent, "radio.frequency", converter)
            .fromClient()
            .nodeString(IRadioChannelAccess::getChannel, IRadioChannelAccess::setChannel)
            .onFinished((tile, target, player) -> tile.setChanged());
    }

    public static <T extends BlockEntity> PacketCodexTile<T, Radio> radioToggleDisable(ResourceLocation parent, Function<T, Radio> converter) {
        return (PacketCodexTile<T, Radio>) new PacketCodexTile<T, Radio>(parent, "radio.disable.toggle", converter)
            .fromClient()
            .toggleBoolean(Radio::isDisabled, Radio::setDisabled)
            .onFinished((tile, target, player) -> tile.setChanged());
    }
}
