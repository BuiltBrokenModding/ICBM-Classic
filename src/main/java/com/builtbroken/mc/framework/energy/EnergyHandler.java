package com.builtbroken.mc.framework.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public abstract class EnergyHandler
{
    public final String moduleName;
    public final String fullUnit;
    public final String unit;

    /**
     * Multiply UE energy by this ratio to convert it to the forgien ratio.
     */
    public double toForeignEnergy;

    /**
     * Multiply the forgien energy by this ratio to convert it into UE energy.
     */
    public double toUEEnergy;

    public EnergyHandler(String moduleName, String fullUnit, String unit, double ratio)
    {
        this.moduleName = moduleName;
        this.fullUnit = fullUnit;
        this.unit = unit;
        this.toForeignEnergy = 1.0 / ratio;
        this.toUEEnergy = ratio;
    }

    public int toUEEnergy(int energy)
    {
        return (int) Math.floor(energy * toForeignEnergy);
    }

    public int toForeignEnergy(int energy)
    {
        return (int) Math.floor(energy * toUEEnergy);
    }

    public final int fromUE(int energy)
    {
        return toForeignEnergy(energy);
    }

    public abstract double receiveEnergy(Object handler, EnumFacing direction, double energy, boolean doReceive);

    public abstract double extractEnergy(Object handler, EnumFacing direction, double energy, boolean doExtract);

    /**
     * Charges an item with the given energy
     *
     * @param itemStack - item stack that is the item
     * @param joules    - input energy
     * @param docharge  - do the action
     * @return amount of energy accepted
     */
    public abstract double chargeItem(ItemStack itemStack, double joules, boolean docharge);

    /**
     * discharges an item with the given energy
     *
     * @param itemStack   - item stack that is the item
     * @param joules      - input energy
     * @param doDischarge - do the action
     * @return amount of energy that was removed
     */
    public abstract double dischargeItem(ItemStack itemStack, double joules, boolean doDischarge);

    public abstract boolean doIsHandler(Object obj, EnumFacing dir);

    public abstract boolean doIsHandler(Object obj);

    public abstract boolean doIsEnergyContainer(Object obj);

    public abstract boolean canConnect(Object obj, EnumFacing direction, Object source);

    public abstract ItemStack getItemWithCharge(ItemStack itemStack, double energy);

    public abstract double getEnergy(Object obj, EnumFacing direction);

    public abstract double getMaxEnergy(Object handler, EnumFacing direction);

    public abstract double getEnergyItem(ItemStack is);

    public abstract double getMaxEnergyItem(ItemStack is);

    public abstract double clearEnergy(Object handler, boolean doAction);

    public abstract double setFullCharge(Object handler);
}