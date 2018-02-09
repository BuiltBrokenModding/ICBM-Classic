package com.builtbroken.mc.api.energy;

/**
 * Simple interface applied to objects that store energy. Normally when exposing this to other object to use the energy is expressed in VE joules.
 * If you are storing in another type then convert before returning.
 * Created by Dark on 8/15/2015.
 */
public interface IEnergyBuffer
{
    /**
     * Added energy to the buffer
     *
     * @param energy - energy to add
     * @return energy stored
     */
    int addEnergyToStorage(int energy, boolean doAction);

    /**
     * Removes energy from the buffer
     *
     * @param energy - energy to remove
     * @return energy actually removed
     */
    int removeEnergyFromStorage(int energy, boolean doAction);

    /**
     * @return Max limit of storage for the buffer
     */
    int getMaxBufferSize();

    /**
     * @return Actual energy stored in buffer
     */
    int getEnergyStored();

    /**
     * Sets the amount of energy stored in the buffer.
     * <p>
     * Do not use this add or remove energy. Instead, use
     * {@link #addEnergyToStorage(int, boolean)} and {@link #removeEnergyFromStorage(int, boolean)}
     * in order to trigger actions and events correctly.
     * <p>
     * This method is only added for edge cases such as packet
     * handling and EMPs.
     *
     * @param energy - energy to set
     */
    void setEnergyStored(int energy);
}
