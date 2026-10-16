package icbm.classic.content.blocks.launcher.connector;

import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileLauncherConnector extends TileLauncherFrame {

    public TileLauncherConnector(BlockPos pos, BlockState state)
    {
        super(BlockReg.TILE_LAUNCHER_CONNECTOR.get(), pos, state);
    }
}
// PORTED
