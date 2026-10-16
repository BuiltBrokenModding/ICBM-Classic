package icbm.classic.lib.radio;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CapabilityRadio implements IRadio {

    @Override
    public BlockPos getBlockPos() {
        return null;
    }

    @Override
    public Level getWorld() {
        return null;
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return null;
    }

    /**
     * 1.12 registered a Forge capability storage + default factory. NeoForge 1.21 capabilities
     * ({@code ICBMClassicAPI.RADIO_CAPABILITY}) need no registration, so this is a no-op kept for callers.
     */
    public static void register()
    {
        // PORT: nothing to register in NeoForge 1.21
    }
}
// PORTED
