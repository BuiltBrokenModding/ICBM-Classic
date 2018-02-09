package com.builtbroken.mc.api.tile.provider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidTank;

/**
 * Allows access to an internal fluid tank
 * <p>
 * Created by Dark on 8/9/2015.
 */
@Deprecated //Being replaced by node/capability system
public interface ITankProvider
{
    /**
     * Gets fluid tank for the type, null
     * should return default tank.
     *
     * @param fluid - fluid to match, can be null
     * @return tanks, or null if can't accept fluid of type
     */
    default IFluidTank getTankForFluid(Fluid fluid)
    {
        return null;
    }

    /**
     * Gets fluid tank for the type, null
     * should return default tank.
     *
     * @param direction - side of the tile being access
     * @param fluid     - fluid to match, can be null
     * @return tanks, or null if can't accept fluid of type
     */
    default IFluidTank getTankForFluid(EnumFacing direction, Fluid fluid)
    {
        return getTankForFluid(fluid);
    }

    /**
     * Check if the tile can be filled
     *
     * @param from
     * @param fluid
     * @return
     */
    default boolean canFill(EnumFacing from, Fluid fluid)
    {
        return getTankForFluid(from, fluid) != null;
    }

    /**
     * Checks if the tile can be drained
     *
     * @param from
     * @param fluid
     * @return
     */
    default boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return getTankForFluid(from, fluid) != null;
    }
}
