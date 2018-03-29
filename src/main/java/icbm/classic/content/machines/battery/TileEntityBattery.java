package icbm.classic.content.machines.battery;

import icbm.classic.lib.IGuiTile;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class TileEntityBattery extends TileMachine implements IInventoryProvider, IGuiTile, IPacketIDReceiver, IPlayerUsing
{
    //Settings
    public static final int SLOTS = 5;

    //Caps
    private ExternalInventory _inventory;
    private IEnergyStorage _batteryWrapper;

    //Gui data
    public int _localEnergy = 0;
    public int _localEnergyMax = 0;

    @Override
    public void update()
    {
        super.update();
    }

    @Override
    public ExternalInventory getInventory()
    {
        if (_inventory == null)
        {
            _inventory = new ExternalInventory(this, SLOTS);
        }
        return _inventory;
    }

    public IEnergyStorage getEnergyStorage()
    {
        if (_batteryWrapper == null)
        {
            _batteryWrapper = new InventoryEnergyStorage(getInventory());
        }
        return _batteryWrapper;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }
        else if (capability == CapabilityEnergy.ENERGY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) getInventory();
        }
        else if (capability == CapabilityEnergy.ENERGY)
        {
            return (T) getEnergyStorage();
        }
        return getCapability(capability, facing);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerBattery(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiBattery(player, this);
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getEnergyStorage().getEnergyStored());
        buf.writeInt(getEnergyStorage().getMaxEnergyStored());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        _localEnergy = buf.readInt();
        _localEnergyMax = buf.readInt();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        getInventory().load(compound.getCompoundTag("inventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger("tier", _tier.ordinal());
        compound.setTag("inventory", getInventory().save(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }
}
