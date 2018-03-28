package icbm.classic.content.machines.battery;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.IGuiTile;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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
public class TileEntityBattery extends TileEntity implements IInventoryProvider, IGuiTile
{
    public static final int SLOTS = 5;

    private ExternalInventory _inventory;
    private IEnergyStorage _batteryWrapper;

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
    public boolean openGui(EntityPlayer player, int requestedID)
    {
        player.openGui(ICBMClassic.INSTANCE, requestedID, world, getPos().getX(), getPos().getY(), getPos().getZ());
        return true;
    }
}
