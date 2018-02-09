package com.builtbroken.mc.framework.energy;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.items.energy.IEnergyBufferItem;
import com.builtbroken.mc.api.items.energy.IEnergyItem;
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
        if (itemStack.getItem() instanceof IEnergyItem)
        {
            int floor = (int) Math.floor(joules);
            return ((IEnergyItem) itemStack.getItem()).recharge(itemStack, floor, docharge);
        }
        else if (itemStack.getItem() instanceof IEnergyBufferItem)
        {
            int e = ((IEnergyBufferItem) itemStack.getItem()).getEnergyCapacity(itemStack);
            int roomLeft = -((IEnergyBufferItem) itemStack.getItem()).getEnergy(itemStack);
            if (joules >= roomLeft)
            {
                if (docharge)
                {
                    ((IEnergyBufferItem) itemStack.getItem()).setEnergy(itemStack, e + roomLeft);
                }
                return roomLeft;
            }
            else
            {
                int newEnergy = (int) Math.floor(e + joules);
                int added = newEnergy - e;
                if (docharge)
                {
                    ((IEnergyBufferItem) itemStack.getItem()).setEnergy(itemStack, newEnergy);
                }
                return added;
            }
        }
        return 0;
    }

    @Override
    public double dischargeItem(ItemStack itemStack, double joules, boolean doDischarge)
    {
        if (itemStack.getItem() instanceof IEnergyItem)
        {
            int floor = (int) Math.floor(joules);
            return ((IEnergyItem) itemStack.getItem()).discharge(itemStack, floor, doDischarge);
        }
        else if (itemStack.getItem() instanceof IEnergyBufferItem)
        {
            int e = ((IEnergyBufferItem) itemStack.getItem()).getEnergyCapacity(itemStack);
            if (joules >= e)
            {
                if (doDischarge)
                {
                    ((IEnergyBufferItem) itemStack.getItem()).setEnergy(itemStack, 0);
                }
                return e;
            }
            else
            {
                int newEnergy = (int) Math.floor(e - joules);
                int removed = e - newEnergy;
                if (doDischarge)
                {
                    ((IEnergyBufferItem) itemStack.getItem()).setEnergy(itemStack, newEnergy);
                }
                return removed;
            }
        }
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
        else if (obj instanceof Item)
        {
            return obj instanceof IEnergyItem || obj instanceof IEnergyBufferItem;
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
        else if (obj instanceof Item)
        {
            return obj instanceof IEnergyBufferItem;
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
        if (itemStack.getItem() instanceof IEnergyItem)
        {
            ((IEnergyItem) itemStack.getItem()).recharge(itemStack, (int) Math.floor(energy), true);
        }
        return itemStack;
    }

    @Override
    public double getEnergy(Object obj, EnumFacing direction)
    {
        if (obj instanceof ItemStack && ((ItemStack) obj).getItem() instanceof IEnergyBufferItem)
        {
            return ((IEnergyBufferItem) ((ItemStack) obj).getItem()).getEnergy((ItemStack) obj);
        }
        else if (obj instanceof IEnergyBufferProvider)
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
        if (obj instanceof ItemStack && ((ItemStack) obj).getItem() instanceof IEnergyBufferItem)
        {
            return ((IEnergyBufferItem) ((ItemStack) obj).getItem()).getEnergyCapacity((ItemStack) obj);
        }
        else if (obj instanceof IEnergyBufferProvider)
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
        if (obj instanceof ItemStack && ((ItemStack) obj).getItem() instanceof IEnergyBufferItem)
        {
            double e = ((IEnergyBufferItem) ((ItemStack) obj).getItem()).getEnergy((ItemStack) obj);
            ((IEnergyBufferItem) ((ItemStack) obj).getItem()).setEnergy((ItemStack) obj, 0);
            return e;
        }
        else if (obj instanceof IEnergyBufferProvider)
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
        else if (handler instanceof ItemStack && ((ItemStack) handler).getItem() instanceof IEnergyBufferItem)
        {
            int energy = ((IEnergyBufferItem) ((ItemStack) handler).getItem()).getEnergy((ItemStack) handler);
            int cap = ((IEnergyBufferItem) ((ItemStack) handler).getItem()).getEnergyCapacity((ItemStack) handler);
            ((IEnergyBufferItem) ((ItemStack) handler).getItem()).setEnergy((ItemStack) handler, cap);
            return cap - energy;
        }
        return receiveEnergy(handler, null, Double.MAX_VALUE, true);
    }
}
