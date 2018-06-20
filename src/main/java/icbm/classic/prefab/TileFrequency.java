package icbm.classic.prefab;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Prefab for tiles that need to receive or send a signal at a Hz value
 */
public abstract class TileFrequency extends TileICBMMachine
{
    /**
     * Frequency of the device
     */
    private int frequency = 0;

    /**
     * Creates a new TileMachine instance
     *
     * @param name     - name of the tile
     * @param material - material of the tile
     */
    public TileFrequency(String name, Material material)
    {
        super(name, material);
    }

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
    public boolean setFrequency(int frequency)
    {
        this.frequency = frequency;
        return true;
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
