package icbm.classic.prefab;

import cofh.api.energy.IEnergyHandler;
import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.tile.ConnectionType;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.mods.ic.ICHandler;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import cpw.mods.fml.common.Optional;
import ic2.api.energy.tile.IEnergySink;
import icbm.classic.mod.IC2Proxy.IC2Proxy;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */

@Optional.InterfaceList({
        @Optional.Interface(iface = "IC2.api.energy.tile.IEnergySink", modid = "IC2"),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHCore")
})
public abstract class TileICBMMachine extends TileModuleMachine implements IEnergyHandler, IEnergySink
{
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
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
    }

    @Override
    protected boolean useMetaForFacing()
    {
        return true;
    }

    public int getEnergy()
    {
        IEnergyBuffer buffer = getEnergyBuffer(ForgeDirection.UNKNOWN);
        if (buffer != null)
        {
            return buffer.getEnergyStored();
        }
        return 0;
    }

    public void setEnergy(int energy)
    {
        IEnergyBuffer buffer = getEnergyBuffer(ForgeDirection.UNKNOWN);
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
        IEnergyBuffer buffer = getEnergyBuffer(ForgeDirection.UNKNOWN);
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

    @Override
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
    @Optional.Method(modid = "IC2")
    public double getDemandedEnergy()
    {
        if (getEnergyBuffer(ForgeDirection.UNKNOWN) != null)
        {
            return ICHandler.FROM_UE * (getEnergyBufferSize() - getEnergy());
        }
        return 0;
    }

    @Override
    @Optional.Method(modid = "IC2")
    public int getSinkTier()
    {
        return 4;
    }

    @Override
    @Optional.Method(modid = "IC2")
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
    {
        IEnergyBuffer buffer = getEnergyBuffer(directionFrom);
        if(buffer != null)
        {
            int energy = (int) Math.floor(amount * ICHandler.TO_UE);
            int received = buffer.addEnergyToStorage(energy, true);
            return amount - (received * ICHandler.FROM_UE);
        }
        return amount;
    }

    @Override
    @Optional.Method(modid = "IC2")
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
    {
        return canConnect(emitter, ConnectionType.POWER, direction);
    }

    @Override
    @Optional.Method(modid = "IC2")
    public void invalidate()
    {
        super.invalidate();
        IC2Proxy.INSTANCE.onTileInvalidate(this);
    }

    @Override
    @Optional.Method(modid = "IC2")
    public void validate()
    {
        super.validate();
        IC2Proxy.INSTANCE.onTileValidate(this);
    }
}
