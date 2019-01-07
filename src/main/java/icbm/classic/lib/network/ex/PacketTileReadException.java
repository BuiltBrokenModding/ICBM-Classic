package icbm.classic.lib.network.ex;

import icbm.classic.lib.transform.vector.Location;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

/**
 * Exception that is thrown when reading a packet with bad data.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/26/2015.
 */
public class PacketTileReadException extends RuntimeException
{
    protected Location location;
    private TileEntity tile;
    private IBlockState block;

    public PacketTileReadException(Location location, String message)
    {
        super(message);
        this.location = location;
        this.tile = location.getTileEntity();
        this.block = location.getBlockState();
    }

    public PacketTileReadException(Location location, String message, Throwable cause)
    {
        super(message, cause);
        this.location = location;
        this.tile = location.getTileEntity();
        this.block = location.getBlockState();
    }

    @Override
    public String toString()
    {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        if (message != null)
        {
            s += ": " + message;
        }
        s += "\nPos: " + location;
        s += "\nTile: " + tile;
        s += "\nBlock: " + block;

        return s;
    }
}
