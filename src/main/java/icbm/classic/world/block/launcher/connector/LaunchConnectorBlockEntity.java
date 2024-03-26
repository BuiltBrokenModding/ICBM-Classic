package icbm.classic.world.block.launcher.connector;

import icbm.classic.world.IcbmBlockEntityTypes;
import icbm.classic.world.block.launcher.frame.LaunchFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LaunchConnectorBlockEntity extends LaunchFrameBlockEntity {

    public LaunchConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(IcbmBlockEntityTypes.LAUNCH_CONNECTOR.get(), pos, state);
    }
}
