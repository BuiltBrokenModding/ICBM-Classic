package icbm.classic.prefab;

import cofh.api.energy.IEnergyHandler;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public abstract class TileICBMMachine extends TileModuleMachine implements IEnergyHandler
{
    /** Energy stored by the machine. */
    protected int energy = 0;
    /**
     * Toggle to send a {@link #getDescPacket()} on the next tick, keep in mind only do this for render data.
     * if the data is not used by the renderer then send it at the time it is needed. For example, GUI data
     * should be sent to only GUI users and not everyone.
     */
    protected boolean updateClient = false;

    /**
     * Creates a new TileMachine instance
     *
     * @param name     - name of the tile
     * @param material - material of the tile
     */
    public TileICBMMachine(String name, Material material)
    {
        super(name, material);
    }

    @Override
    public void update()
    {
        super.update();
        doUpdateGuiUsers();
    }

    @Override
    public void doUpdateGuiUsers()
    {
        if (isServer())
        {
            //Sync client(s) if needed
            if (updateClient)
            {
                updateClient = false;
                sendDescPacket();
            }
            //Sync GUI data to client(s)
            if (ticks % 3 == 0)
            {
                PacketTile packet = getGUIPacket();
                if (packet != null)
                {
                    sendPacketToGuiUsers(packet);
                }
            }
        }
    }

    /**
     * Packet sent to GUI users
     *
     * @return
     */
    protected PacketTile getGUIPacket()
    {
        return null;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
    {
        int space = getMaxEnergyStored(from) - getEnergyStored(from);
        if (space >= maxReceive)
        {
            if (!simulate)
            {
                energy += maxReceive;
            }
            return maxReceive;
        }
        else
        {
            if (!simulate)
            {
                energy += space;
            }
            return space;
        }
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
    {
        if (maxExtract >= energy)
        {
            if (!simulate)
            {
                energy = 0;
            }
            return energy;
        }
        else
        {
            if (!simulate)
            {
                energy -= maxExtract;
            }
            return maxExtract;
        }
    }

    @Override
    public int getEnergyStored(ForgeDirection from)
    {
        return energy;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return 10000;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from)
    {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("energy", energy);
    }

    @Override
    protected boolean useMetaForFacing()
    {
        return true;
    }

    /**
     * Called to extract the amount of energy the machine needs to use per operation
     */
    public void extractEnergy()
    {
        extractEnergy(ForgeDirection.UNKNOWN, getEnergyConsumption(), false);
    }

    /**
     * Called to check if the machine has enough energy to operate
     *
     * @return true if yes
     */
    public boolean checkExtract()
    {
        return getEnergyStored(ForgeDirection.UNKNOWN) >= getEnergyConsumption();
    }

    /**
     * How much energy does this machine consume per operation
     *
     * @return
     */
    public int getEnergyConsumption()
    {
        return (int) (getMaxEnergyStored(ForgeDirection.UNKNOWN) * .9);
    }

    /**
     * Do we have any amount of power stored.
     *
     * @return true if greater than zero or other condition.
     */
    public boolean hasPower()
    {
        return getEnergyStored(ForgeDirection.UNKNOWN) > 0;
    }
}
