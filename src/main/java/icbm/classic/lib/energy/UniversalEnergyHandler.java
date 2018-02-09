package icbm.classic.lib.energy;

import icbm.classic.api.energy.IEnergyBuffer;
import icbm.classic.api.energy.IEnergyBufferProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 8/7/2017.
 */
public class UniversalEnergyHandler extends EnergyHandler
{
    public UniversalEnergyHandler()
    {
        super("ue", "watts", "watts", 1);
    }

    @Override
    public double receiveEnergy(Object handler, EnumFacing direction, double energy, boolean doReceive)
    {
        if (handler instanceof ItemStack)
        {
            return chargeItem((ItemStack) handler, energy, doReceive);
        }
        else if (handler instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) handler).getEnergyBuffer(direction);
            if (buffer != null)
            {
                int floor = (int) Math.floor(energy);
                return buffer.addEnergyToStorage(floor, doReceive);
            }
        }
        return 0;
    }

    @Override
    public double extractEnergy(Object handler, EnumFacing direction, double energy, boolean doExtract)
    {
        if (handler instanceof ItemStack)
        {
            return dischargeItem((ItemStack) handler, energy, doExtract);
        }
        else if (handler instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) handler).getEnergyBuffer(direction);
            if (buffer != null)
            {
                int floor = (int) Math.floor(energy);
                return buffer.removeEnergyFromStorage(floor, doExtract);
            }
        }
        return 0;
    }

    @Override
    public double chargeItem(ItemStack itemStack, double joules, boolean docharge)
    {
        return 0;
    }

    @Override
    public double dischargeItem(ItemStack itemStack, double joules, boolean doDischarge)
    {
        return 0;
    }

    @Override
    public boolean doIsHandler(Object obj, EnumFacing dir)
    {
        return doIsHandler(obj);
    }

    @Override
    public boolean doIsHandler(Object obj)
    {
        if (obj instanceof ItemStack)
        {
            Item item = ((ItemStack) obj).getItem();
            return doIsHandler(item);
        }
        return obj instanceof IEnergyBufferProvider;
    }

    @Override
    public boolean doIsEnergyContainer(Object obj)
    {
        if (obj instanceof ItemStack)
        {
            Item item = ((ItemStack) obj).getItem();
            return doIsHandler(item);
        }
        return obj instanceof IEnergyBufferProvider;
    }

    @Override
    public boolean canConnect(Object obj, EnumFacing direction, Object source)
    {
        return true;
    }

    @Override
    public ItemStack getItemWithCharge(ItemStack itemStack, double energy)
    {
        return itemStack;
    }

    @Override
    public double getEnergy(Object obj, EnumFacing direction)
    {
        if (obj instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) obj).getEnergyBuffer(direction);
            if (buffer != null)
            {
                return buffer.getEnergyStored();
            }
        }
        return 0;
    }

    @Override
    public double getMaxEnergy(Object obj, EnumFacing direction)
    {
        if (obj instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) obj).getEnergyBuffer(direction);
            if (buffer != null)
            {
                return buffer.getMaxBufferSize();
            }
        }
        return 0;
    }

    @Override
    public double getEnergyItem(ItemStack is)
    {
        return getEnergy(is, null);
    }

    @Override
    public double getMaxEnergyItem(ItemStack is)
    {
        return getMaxEnergy(is, null);
    }

    @Override
    public double clearEnergy(Object obj, boolean doAction)
    {
        if (obj instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) obj).getEnergyBuffer(null);
            if (buffer != null)
            {
                double e = buffer.getEnergyStored();
                buffer.setEnergyStored(0);
                return e;
            }
        }
        return 0;
    }

    @Override
    public double setFullCharge(Object handler)
    {
        if (handler instanceof IEnergyBufferProvider)
        {
            IEnergyBuffer buffer = ((IEnergyBufferProvider) handler).getEnergyBuffer(null);
            if (buffer != null)
            {
                int energy = buffer.getEnergyStored();
                buffer.setEnergyStored(buffer.getMaxBufferSize());
                return buffer.getEnergyStored() - energy;
            }
        }
        return receiveEnergy(handler, null, Double.MAX_VALUE, true);
    }
}
