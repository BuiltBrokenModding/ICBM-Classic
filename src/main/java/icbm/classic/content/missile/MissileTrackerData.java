package icbm.classic.content.missile;

import com.builtbroken.jlib.data.vector.Pos3D;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;

public class MissileTrackerData
{
    public int ticksLeftToTarget;
    public Pos3D targetPos;
    public Explosives explosiveID;

    public void readFromNBT(NBTTagCompound nbt)
    {
        ticksLeftToTarget = nbt.getInteger("ticksLeftToTarget");
        int[] pos = nbt.getIntArray("targetPosX");
        targetPos = new Pos(pos[0], pos[1], pos[2]);
        explosiveID = Explosives.values()[nbt.getInteger("explosiveID")];
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("ticksLeftToTarget",ticksLeftToTarget);
        nbt.setIntArray("targetPosX", new int[]{targetPos.xi(), targetPos.yi(), targetPos.zi()});
        nbt.setInteger("explosiveID",explosiveID.ordinal());
        return nbt;
    }
}
