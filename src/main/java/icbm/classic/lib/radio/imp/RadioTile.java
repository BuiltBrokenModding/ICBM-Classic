package icbm.classic.lib.radio.imp;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.lib.radio.RadioRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class RadioTile<T extends TileEntity> extends Radio {

    protected final T host;

    public RadioTile(T host) {
        this.host = host;
    }

    @Override
    public BlockPos getBlockPos() {
        return host.getPos();
    }

    @Override
    public World getWorld() {
        return host.getWorld();
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
            && host.hasWorld() && !host.getWorld().isRemote
            // Validate channel, this might create the channel string if null
            && Objects.equals(getChannel(), packet.getChannel());
    }
}
