package icbm.classic.lib.energy.system;

import net.minecraft.util.EnumFacing;

/**
 * Wrapper for energy systems
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
public interface IEnergySystem
{
    /**
     * Can this power system support the object
     *
     * @param object - ItemStack/Entity/TileEntity
     * @param side   - side being accessed, can be null
     * @return true if method calls work on the object
     */
    boolean canSupport(Object object, EnumFacing side);

    /**
     * Attempts to update the energy stored in the object
     *
     * @param object   - ItemStack/Entity/TileEntity
     * @param side     - side being accessed, can be null
     * @param energy   - new value
     * @param simulate - true to run logic without applying changes
     * @return energy previously contained
     */
    int setEnergy(Object object, EnumFacing side, int energy, boolean simulate);

    /**
     * Check to see if the energy system supports setting
     * energy directly on the object.
     *
     * @param object - ItemStack/Entity/TileEntity
     * @param side   - side being accessed, can be null
     * @return true if can support feature
     */
    boolean canSetEnergyDirectly(Object object, EnumFacing side);


    /**
     * Called to get the energy stores in the object
     *
     * @param object - ItemStack/Entity/TileEntity
     * @param side   - side being accessed, can be null
     * @return energy contained
     */
    int getEnergy(Object object, EnumFacing side);

    /**
     * Called to get the limit of energy storage
     *
     * @param object - ItemStack/Entity/TileEntity
     * @param side   - side being accessed, can be null
     * @return energy limit
     */
    int getCapacity(Object object, EnumFacing side);

    /**
     * Attempts to add energy to the object
     *
     * @param object      - ItemStack/Entity/TileEntity
     * @param side        - side being accessed, can be null
     * @param energyToAdd - energy to add
     * @param simulate    - true to run logic without applying changes
     * @return energy added
     */
    int addEnergy(Object object, EnumFacing side, int energyToAdd, boolean simulate);

    /**
     * Attempts to remove energy from the object
     *
     * @param object         - ItemStack/Entity/TileEntity
     * @param side           - side being accessed, can be null
     * @param energyToRemove - energy to remove
     * @param simulate       - true to run logic without applying changes
     * @return energy removed
     */
    int removeEnergy(Object object, EnumFacing side, int energyToRemove, boolean simulate);
}
