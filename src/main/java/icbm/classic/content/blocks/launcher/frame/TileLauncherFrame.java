package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class TileLauncherFrame extends TileEntity implements ILauncherComponent {

    private final LauncherNode launcherNode = new LauncherNode(this, false);

    public TileLauncherFrame(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void onLoad()
    {
       launcherNode.connectToTiles();
    }

    @Override
    public void remove()
    {
        getNetworkNode().onTileRemoved();
        super.remove();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (getNetworkNode().getNetwork() != null)
        {
            final LazyOptional<T> cap = getNetworkNode().getNetwork().getCapability(capability, facing);
            if(cap.isPresent()) {
                return cap;
            }
        }
        return super.getCapability(capability, facing);
    }
}
