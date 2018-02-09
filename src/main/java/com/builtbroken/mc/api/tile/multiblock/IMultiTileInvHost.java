package com.builtbroken.mc.api.tile.multiblock;

import net.minecraft.inventory.IInventory;

/** Provides access to inventories per multiblock segment allowing more control over inventory access
 * Created by Robert on 8/10/2015.
 */
public interface IMultiTileInvHost
{
    /**
     * Grabs an inventory for the multiblock segment
     * @param tile - multiblock segment to check against
     * @return IInventory or null if the tile has no inventory to access
     */
    IInventory getInventoryForTile(IMultiTile tile);
}
