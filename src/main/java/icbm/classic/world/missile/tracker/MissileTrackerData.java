package icbm.classic.world.missile.tracker;

import icbm.classic.datafix.EntityMissileDataFixer;
import icbm.classic.lib.NBTConstants;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Stores missile simulation Data
 * <p>
 * Created by GHXX on 8/4/2018.
 */

public class MissileTrackerData {
    public int preLoadChunkTimer;   //Seconds before the missiles spawns in the loaded chunk

    public int ticksLeftToTarget;   //Seconds left before the missile reaches the target area (1 Tick = 1 Second)
    public Pos targetPos;           //Target coordinates

    public CompoundTag missileData;  //Additional missile data

    //Constructors
    public MissileTrackerData(ExplosiveMissileEntity missile) {
        targetPos = new Pos(missile.getMissileCapability().getTargetData().getPosition()); //TODO switch to storing targeting data
        missileData = new CompoundTag();
        missile.writeToNBTAtomically(missileData);
        missileData.remove("Pos");
    }

    public MissileTrackerData(CompoundTag tagCompound) {
        readFromNBT(tagCompound);
    }

    //Helper methods for saving and loading
    public void readFromNBT(CompoundTag nbt) {
        ticksLeftToTarget = nbt.getInteger(NBTConstants.TICKS);
        targetPos = new Pos(nbt.getCompound(NBTConstants.TARGET));

        missileData = nbt.getCompound(NBTConstants.DATA);

        // Fix old saves, [< 4.2.0] didn't include id and is using the pre-missile rewrite data
        if (!missileData.contains("id")) {
            missileData.putString("id", "icbmclassic:missile");
            missileData = EntityMissileDataFixer.INSTANCE.fixTagCompound(missileData);
        }
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setInteger(NBTConstants.TICKS, ticksLeftToTarget);
        nbt.put(NBTConstants.TARGET, targetPos.writeNBT(new CompoundTag()));
        nbt.put(NBTConstants.DATA, missileData);
        return nbt;
    }
}
