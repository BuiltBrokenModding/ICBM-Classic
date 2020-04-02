package icbm.classic.content.entity.missile;

import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores missile simulation Data
 *
 * Created by GHXX on 8/4/2018.
 */

public class MissileTrackerData
{
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
        ticksLeftToTarget = nbt.getInteger(NBTConstants.TICKS);
        targetPos = new Pos(nbt.getCompoundTag(NBTConstants.TARGET));
        missileData = nbt.getCompoundTag(NBTConstants.DATA);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.TICKS, ticksLeftToTarget);
        nbt.setTag(NBTConstants.TARGET, targetPos.writeNBT(new NBTTagCompound()));
        nbt.setTag(NBTConstants.DATA, missileData);
        return nbt;
    }
}
