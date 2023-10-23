package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileLauncherFrame extends TileEntity implements ILauncherComponent {

    private final LauncherNode launcherNode = new LauncherNode(this, false);

    @Override
    public void onLoad()
    {
       launcherNode.connectToTiles();
    }

    @Override
    public void invalidate()
    {
        getNetworkNode().onTileRemoved();
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing) || Optional.ofNullable(getNetworkNode().getNetwork()).map(network -> network.hasCapability(capability, facing)).orElse(false);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (getNetworkNode().getNetwork() != null)
        {
            final T cap = getNetworkNode().getNetwork().getCapability(capability, facing);
            if(cap != null) {
                return cap;
            }
        }
        return super.getCapability(capability, facing);
    }
}
