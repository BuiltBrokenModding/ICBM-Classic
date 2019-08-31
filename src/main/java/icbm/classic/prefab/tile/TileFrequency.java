package icbm.classic.prefab.tile;

import icbm.classic.api.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Prefab for tiles that need to receive or send a signal at a Hz value
 */
public abstract class TileFrequency extends TilePoweredMachine
{
    /**
     * Frequency of the device
     */
    private int frequency = 0;

    /**
     * What is the frequency of the device
     *
     * @return Hz value
     */
    public int getFrequency()
    {
        return this.frequency;
    }

    /**
     * Called to se the frequency of the device
     *
     * @param frequency - Hz value
     */
    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.frequency = nbt.getInteger(NBTConstants.FREQUENCY);
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.FREQUENCY, this.frequency);
        return super.writeToNBT(nbt);
    }
}
