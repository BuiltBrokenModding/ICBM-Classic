package com.builtbroken.mc.api.map.radio.wireless;

/**
 * Different reason why a connection is lost for a wireless network.
 * <p>
 * In most cases do nothing other than clear cached data about the object. Only
 * clear all data if the tile is removed from the world. As this is the only
 * case where the tile will not come back online. In other cases the tile
 * will come back online and the player will get upset if he has to
 * recode data into GUIs.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public enum ConnectionRemovedReason
{
    /** Tile was invalidated by the game, any number of reasons why.. */
    TILE_INVALIDATE,
    /** Tile was destroyed by a player, entity, explosion, or machine */
    DESTROYED,
    /** World unloaded, do nothing but clear cache */
    WORLD_UNLOAD,
    /** Chunk unloaded, do nothing but clear cache */
    CHUNK_UNLOAD,
    /** Generic connection lost, most likely power failure or radio range change */
    CONNECTION_LOST;
}
