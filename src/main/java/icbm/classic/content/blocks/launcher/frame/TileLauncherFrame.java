package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;

import javax.annotation.Nullable;

/**
 * Launcher frame, passes the launcher network's energy and inventory capabilities through.
 * <p>
 * PORT: LauncherNode#setNetwork invalidates the cached capabilities at this position.
 */
public class TileLauncherFrame extends BlockEntity implements ILauncherComponent, ICapabilityHolderTile {

    private final LauncherNode launcherNode = new LauncherNode(this, false);

    public TileLauncherFrame(BlockPos pos, BlockState state)
    {
        this(BlockReg.TILE_LAUNCHER_FRAME.get(), pos, state);
    }

    protected TileLauncherFrame(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        launcherNode.connectToTiles();
    }

    @Override
    public void setRemoved()
    {
        getNetworkNode().onTileRemoved();
        super.setRemoved();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        if (getNetworkNode().getNetwork() != null)
        {
            return getNetworkNode().getNetwork().getCapability(capability, facing);
        }
        return null;
    }
}
// PORTED
