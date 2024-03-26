package icbm.classic.world.block.launcher.frame;

import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Optional;

public class LaunchFrameBlockEntity extends BaseContainerBlockEntity implements ILauncherComponent {

    private final LauncherNode launcherNode = new LauncherNode(this, false);

    public LaunchFrameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        launcherNode.connectToTiles();
    }

    @Override
    public void invalidate() {
        getNetworkNode().onTileRemoved();
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return super.hasCapability(capability, facing) || Optional.ofNullable(getNetworkNode().getNetwork()).map(network -> network.hasCapability(capability, facing)).orElse(false);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (getNetworkNode().getNetwork() != null) {
            final T cap = getNetworkNode().getNetwork().getCapability(capability, facing);
            if (cap != null) {
                return cap;
            }
        }
        return super.getCapability(capability, facing);
    }
}
