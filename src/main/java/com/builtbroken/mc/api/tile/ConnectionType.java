package com.builtbroken.mc.api.tile;

/**
 * Used to filter connection types
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 8/11/2016.
 */
public enum ConnectionType
{
    /** Same as unknown, or other */
    UNSPECIFIC,
    /** Any type of power connection */
    POWER,
    /** Any type of fluid connection */
    FLUID,
    /** Inventory item inputs */
    INVENTORY,
    /** Power from RF API */
    RF_POWER,
    /** Power from IC2 API */
    IC_POWER,
    /** Power from UE API */
    UE_POWER,
    /** Power from tesla API */
    TESLA_POWER,
    /** Gas type fluids only */
    GAS,
    /** Liquid type fluids only */
    LIQUID

}
