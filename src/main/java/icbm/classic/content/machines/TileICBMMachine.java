package icbm.classic.content.machines;

import cofh.api.energy.IEnergyHandler;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class TileICBMMachine extends TileModuleMachine implements IEnergyHandler
{
    protected int energy = 0;

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

    public void extractEnergy()
    {
        extractEnergy(ForgeDirection.UNKNOWN, getEnergyConsumption(), false);
    }

    public boolean checkExtract()
    {
        return getEnergyStored(ForgeDirection.UNKNOWN) >= getEnergyConsumption();
    }

    public int getEnergyConsumption()
    {
        return (int) (getMaxEnergyStored(ForgeDirection.UNKNOWN) * .9);
    }

    public boolean hasPower()
    {
        return getEnergyStored(ForgeDirection.UNKNOWN) > 0;
    }
}
