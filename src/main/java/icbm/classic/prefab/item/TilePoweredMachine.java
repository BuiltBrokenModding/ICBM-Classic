package icbm.classic.prefab.item;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.framework.energy.EnergyBuffer;
import icbm.classic.prefab.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

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
        IEnergyBuffer buffer = getEnergyBuffer(null);
        if (buffer != null)
        {
            return buffer.getEnergyStored();
        }
        return 0;
    }

    public void setEnergy(int energy)
    {
        IEnergyBuffer buffer = getEnergyBuffer(null);
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
        IEnergyBuffer buffer = getEnergyBuffer(null);
        if (buffer != null)
        {
            buffer.removeEnergyFromStorage(getEnergyConsumption(), true);
        }
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getEnergy());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        setEnergy(buf.readInt());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        setEnergy(compound.getInteger("energy"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger("energy", getEnergy());
        return super.writeToNBT(compound);
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
    public IEnergyBuffer getEnergyBuffer(EnumFacing side)
    {
        if (buffer == null)
        {
            buffer = new PowerBuffer(this);
        }
        return buffer;
    }

    public static class PowerBuffer extends EnergyBuffer
    {
        TilePoweredMachine machine;

        public PowerBuffer(TilePoweredMachine machine)
        {
            super(machine.getEnergyBufferSize());
            this.machine = machine;
        }

        @Override
        public int getMaxBufferSize()
        {
            return machine.getEnergyBufferSize();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
        {
            return (T) getEnergyBuffer(facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }
}
