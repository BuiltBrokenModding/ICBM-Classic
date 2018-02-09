package com.builtbroken.mc.api.tile.multiblock;

/**
 * Created by Dark on 8/9/2015.
 */
public interface IMultiTile
{
    /** @return the tile that hosts the functionality of the multi block structure */
    IMultiTileHost getHost();

    /**
     * Sets the host for this multi block peace.
     * Should never be called by anything other than the multi block host
     *
     * @param host
     */
    void setHost(IMultiTileHost host);
}
