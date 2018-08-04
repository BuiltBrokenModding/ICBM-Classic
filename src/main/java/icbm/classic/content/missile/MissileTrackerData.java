package icbm.classic.content.missile;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;

public class MissileTrackerData
{
    private static final String NBT_TICKS = "ticks";
    private static final String NBT_TARGET = "target";
    private static final String NBT_MISSILE_DATA = "data";

    public int preLoadChunkTimer;

    public int ticksLeftToTarget;
    public Pos targetPos;

    public NBTTagCompound missileData;

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
