package icbm.classic.prefab;

import icbm.classic.content.machines.TileICBMMachine;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileFrequency extends TileICBMMachine
{
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
