package icbm.classic.content.blocks.multiblock;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.config.ConfigIC2;
import icbm.classic.mods.ic2.IC2Proxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Basic implementation of a multi block
 * Created by Dark on 8/9/2015.
 */
@Optional.InterfaceList({
    @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2"),
    @Optional.Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "ic2")
})
public class TileMulti extends TileEntity implements IMultiTile, IEnergySink
{
    private WeakReference<IMultiTileHost> hostWeakReference;
    private BlockPos hostPosition = null;

    @Override
    public IMultiTileHost getHost()
    {
        if (isHostLoaded())
        {
            if (hostWeakReference != null && hostWeakReference.get() != null)
            {
                return hostWeakReference.get();
            }
            else if (hostPosition != null && world.isBlockLoaded(hostPosition))
            {
                TileEntity tile = world.getTileEntity(hostPosition);
                if (tile instanceof IMultiTileHost)
                {
                    setHost((IMultiTileHost) tile);
                    return (IMultiTileHost) tile;
                }
            }
        }
        return null;
    }

    public boolean hasHost()
    {
        return hostPosition != null;
    }

    public boolean isHostLoaded()
    {
        return hostPosition != null && world.isBlockLoaded(hostPosition);
    }

    @Override
    public boolean isHost(IMultiTileHost host)
    {
        return host == getHost() || hostPosition != null && hostPosition.equals(host.getPos());
    }

    @Override
    public void setHost(IMultiTileHost host)
    {
        if (host != null)
        {
            hostWeakReference = new WeakReference(host);
            hostPosition = new BlockPos(host.xi(), host.yi(), host.zi());
        }
        else
        {
            if (hostWeakReference != null)
            {
                hostWeakReference.clear();
                hostWeakReference = null;
            }
            hostPosition = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(NBTConstants.HOST_POS))
        {
            int[] data = compound.getIntArray(NBTConstants.HOST_POS);
            hostPosition = new BlockPos(data[0], data[1], data[2]);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        if (hostPosition != null)
        {
            compound.setIntArray(NBTConstants.HOST_POS, new int[]{hostPosition.getX(), hostPosition.getY(), hostPosition.getZ()});
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void invalidate()
    {
        if (getHost() != null)
        {
            getHost().onTileInvalidate(this);
        }
        super.invalidate();
        IC2Proxy.INSTANCE.onTileInvalidate(this);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (getHost() instanceof TileEntity)
        {
            return ((TileEntity) getHost()).hasCapability(capability, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (getHost() instanceof TileEntity)
        {
            return ((TileEntity) getHost()).getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[ DIM@" + (world != null && world.provider != null ? world.provider.getDimension() + " " : "null ") + getPos().getX() + "x " + getPos().getY() + "y " + getPos().getZ() + "z " + "]@" + hashCode();
    }



    @Override
    @Optional.Method(modid = "ic2")
    public double getDemandedEnergy()
    {
        if (!ConfigIC2.DISABLED && hasCapability(CapabilityEnergy.ENERGY, null))
        {
            IEnergyStorage energyStorage = getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage != null)
            {
                int need = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
                return need / ConfigIC2.FROM_IC2;
            }
        }
        return 0;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public int getSinkTier()
    {
        return !ConfigIC2.DISABLED ? 4 : 0;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage)
    {
        if (!ConfigIC2.DISABLED && hasCapability(CapabilityEnergy.ENERGY, null))
        {
            IEnergyStorage energyStorage = getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage != null)
            {
                int energy = (int) Math.floor(amount * ConfigIC2.FROM_IC2);
                int received = energyStorage.receiveEnergy(energy, false);
                return amount - (received / ConfigIC2.FROM_IC2);
            }
        }
        return amount;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side)
    {
        return !ConfigIC2.DISABLED && hasCapability(CapabilityEnergy.ENERGY, side);
    }

    @Override
    public void validate()
    {
        super.validate();
        IC2Proxy.INSTANCE.onTileValidate(this);
    }
}
