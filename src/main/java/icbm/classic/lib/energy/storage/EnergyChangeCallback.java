package icbm.classic.lib.energy.storage;

@FunctionalInterface
public interface EnergyChangeCallback {

    /**
     * Called when the power changes in the buffer
     *
     * @param prev    energy before action
     * @param current energy after action
     * @param reason  to note why the energy was changed
     */
    void onChange(int prev, int current, EnergyActionType reason);
}
