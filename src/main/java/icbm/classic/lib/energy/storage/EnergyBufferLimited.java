package icbm.classic.lib.energy.storage;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class EnergyBufferLimited extends EnergyBuffer
{
    public int inputLimit;
    public int outputLimit;

    public EnergyBufferLimited(int capacity, int inputLimit, int outputLimit)
    {
        super(capacity);
        this.inputLimit = inputLimit;
        this.outputLimit = outputLimit;
    }

    @Override
    public int addEnergyToStorage(int energy, boolean doAction)
    {
        return super.addEnergyToStorage(Math.min(energy, inputLimit), doAction);
    }

    @Override
    public int removeEnergyFromStorage(int energy, boolean doAction)
    {
        return super.removeEnergyFromStorage(Math.min(energy, outputLimit), doAction);
    }
}
