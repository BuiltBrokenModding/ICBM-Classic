package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.reg.TileReg;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class TileLauncherFrame extends TileEntity implements ILauncherComponent, ITickableTileEntity {
    @Deprecated //TODO pull from block registry name
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "launcher_frame");

    private final LauncherNode launcherNode = new LauncherNode(this, false);

    private boolean init = false;

    public TileLauncherFrame() {
        super(TileReg.LAUNCHER_FRAME.get());
    }

    public TileLauncherFrame(TileEntityType<?> tileEntityTypeIn) { //TODO make abstract tile that frame extends
        super(tileEntityTypeIn);
    }

    @Override
    public void tick()
    {
        // whatever reason onLoad can't be used for tile checks
        if(!world.isRemote && !this.init) {
            this.init = true;
            launcherNode.connectToTiles();
        }
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
