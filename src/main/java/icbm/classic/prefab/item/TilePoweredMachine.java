package icbm.classic.prefab.item;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.framework.energy.data.EnergyBuffer;
import icbm.classic.prefab.TileMachine;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/7/2018.
 */
public class TilePoweredMachine extends TileMachine implements IEnergyBufferProvider
{
    EnergyBuffer buffer;

    public int getEnergy()
    {
        IEnergyBuffer buffer = getEnergyBuffer(Direction.UNKNOWN);
        if (buffer != null)
        {
            return buffer.getEnergyStored();
        }
        return 0;
    }

    public void setEnergy(int energy)
    {
        IEnergyBuffer buffer = getEnergyBuffer(Direction.UNKNOWN);
        if (buffer != null)
        {
            buffer.setEnergyStored(energy);
        }
    }

    /**
     * Called to extract the amount of power the machine needs to use per operation
     */
    public void extractEnergy()
    {
        IEnergyBuffer buffer = getEnergyBuffer(Direction.UNKNOWN);
        if (buffer != null)
        {
            buffer.removeEnergyFromStorage(getEnergyConsumption(), true);
        }
    }

    /**
     * Called to check if the machine has enough power to operate
     *
     * @return true if yes
     */
    public boolean checkExtract()
    {
        return getEnergy() >= getEnergyConsumption();
    }

    /**
     * How much power does this machine consume per operation
     *
     * @return
     */
    public int getEnergyConsumption()
    {
        return 10000;
    }

    public int getEnergyBufferSize()
    {
        return getEnergyConsumption() * 2;
    }

    /**
     * Do we have any amount of power stored.
     *
     * @return true if greater than zero or other condition.
     */
    public boolean hasPower()
    {
        return getEnergy() > 0;
    }

    @Override
    public IEnergyBuffer getEnergyBuffer(Direction side)
    {
        if (buffer == null)
        {
            buffer = new EnergyBuffer(getEnergyBufferSize());
        }
        return buffer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        //TODO wrapper CapabilityEnergy.ENERGY to IEnergyBuffer
        return super.getCapability(capability, facing);
    }
}
