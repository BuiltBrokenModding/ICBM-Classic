package icbm.classic.lib.radio.imp;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.lib.radio.RadioRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class RadioTile<T extends BlockEntity> extends Radio {

    protected final T host;

    public RadioTile(T host) {
        this.host = host;
    }

    @Override
    public BlockPos getBlockPos() {
        return host.getPos();
    }

    @Override
    public Level getLevel() {
        return host.getLevel();
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return RadioRegistry.INFINITE;
    }

    public boolean canReceive(IRadioSender sender, IRadioMessage packet) {
        return !isDisabled()
            // Usually sender isn't receive, but could happen in rare cases
            && sender != this
            // Only accept server side
            && host.hasLevel() && !host.getLevel().isClientSide()
            // Validate channel, this might create the channel string if null
            && Objects.equals(getChannel(), packet.getChannel());
    }
}
