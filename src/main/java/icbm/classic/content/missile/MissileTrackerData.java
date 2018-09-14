package icbm.classic.content.missile;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores missile simulation Data
 *
 * Created by GHXX on 8/4/2018.
 */

public class MissileTrackerData
{
    private static final String NBT_TICKS = "ticks";
    private static final String NBT_TARGET = "target";
    private static final String NBT_MISSILE_DATA = "data";

    public int preLoadChunkTimer;   //Seconds before the missiles spawns in the loaded chunk

    public int ticksLeftToTarget;   //Seconds left before the missile reaches the target area (1 Tick = 1 Second)
    public Pos targetPos;           //Target coordinates

    public NBTTagCompound missileData;  //Additional missile data

    //Constructors
    public MissileTrackerData(EntityMissile missile)
    {
        targetPos = missile.targetPos;
        missileData = missile.writeToNBT(new NBTTagCompound());
        missileData.removeTag("Pos");
    }

    public MissileTrackerData(NBTTagCompound tagCompound)
    {
        readFromNBT(tagCompound);
    }

    //Helper methods for saving and loading
    public void readFromNBT(NBTTagCompound nbt)
    {
        ticksLeftToTarget = nbt.getInteger(NBT_TICKS);
        targetPos = new Pos(nbt.getCompoundTag(NBT_TARGET));
        missileData = nbt.getCompoundTag(NBT_MISSILE_DATA);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBT_TICKS, ticksLeftToTarget);
        nbt.setTag(NBT_TARGET, targetPos.writeNBT(new NBTTagCompound()));
        nbt.setTag(NBT_MISSILE_DATA, missileData);
        return nbt;
    }
}
