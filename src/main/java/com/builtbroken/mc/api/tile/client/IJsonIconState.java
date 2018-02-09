package com.builtbroken.mc.api.tile.client;

/**
 * Used by blocks to control the state ID for the given block side.
 * <p>
 * Idea use for this interface is when a tile or node requires more details logic for icons.
 * Ex. energy level, fluid tank, animations
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/30/2017.
 */
public interface IJsonIconState
{
    /**
     * Gets the content state ID for the side of the block.
     *
     * @param side - side of the block
     * @param meta - passed in from icon method
     * @return unique content state ID
     */
    String getContentStateForSide(int side, int meta);
}
