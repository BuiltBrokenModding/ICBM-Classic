package icbm.classic;

import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import net.minecraft.tileentity.TileEntity;

/**
 * Used for testing multi-block calls without an actual multi-block
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 12/15/2019.
 */
public final class DummyMultiTile extends TileEntity implements IMultiTile
{

    private IMultiTileHost host;

    @Override
    public IMultiTileHost getHost()
    {
        return host;
    }

    @Override
    public void setHost(IMultiTileHost host)
    {
        this.host = host;
    }

    @Override
    public boolean isHost(IMultiTileHost host)
    {
        return host == this.host;
    }
}
