package icbm.classic.prefab;

import net.minecraft.nbt.NBTTagCompound;

public abstract class TileFrequency extends TileICBM
{
    private int frequency = 0;


    public int getFrequency()
    {
        return this.frequency;
    }


    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.frequency = nbt.getInteger("frequency");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("frequency", this.frequency);
    }
}
