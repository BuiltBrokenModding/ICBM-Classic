package icbm.classic.content.missile.tracker;

import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

/**
 * Stores missile simulation Data
 *
 * Created by GHXX on 8/4/2018.
 */

public class MissileTrackerData
{
    public int preLoadChunkTimer;   //Seconds before the missiles spawns in the loaded chunk
    public int spawnRetries;        //PORT: times the spawn was delayed because the target chunk was not ticking entities yet, not saved

    public int ticksLeftToTarget;   //Seconds left before the missile reaches the target area (1 Tick = 1 Second)
    public Pos targetPos;           //Target coordinates

    public CompoundTag missileData;  //Additional missile data

    //Constructors
    public MissileTrackerData(EntityExplosiveMissile missile)
    {
        targetPos = new Pos(missile.getMissileCapability().getTargetData().getPosition()); //TODO switch to storing targeting data
        missileData = new CompoundTag();
        // PORT: 1.12 writeToNBTAtomically -> full save plus entity id
        missile.saveWithoutId(missileData);
        missileData.putString("id", EntityType.getKey(missile.getType()).toString());
        missileData.remove("Pos");
        missileData.remove("UUID"); // PORT: entity is re-created with a fresh UUID on spawn, 1.12 readEntityFromNBT never applied the UUID
    }

    public MissileTrackerData(CompoundTag tagCompound)
    {
        readFromNBT(tagCompound);
    }

    //Helper methods for saving and loading
    public void readFromNBT(CompoundTag nbt)
    {
        ticksLeftToTarget = nbt.getInt(NBTConstants.TICKS);
        targetPos = new Pos(nbt.getCompound(NBTConstants.TARGET));

        missileData = nbt.getCompound(NBTConstants.DATA);

        // Fix old saves, [< 4.2.0] didn't include id and is using the pre-missile rewrite data
        if(!missileData.contains("id")) {
            missileData.putString("id", "icbmclassic:missile");
            // PORT-TODO: EntityMissileDataFixer (pre 4.2.0 1.12 save data) was not ported, such data cannot reach a 1.21 world
        }
    }

    public CompoundTag writeToNBT(CompoundTag nbt)
    {
        nbt.putInt(NBTConstants.TICKS, ticksLeftToTarget);
        nbt.put(NBTConstants.TARGET, targetPos.writeNBT(new CompoundTag()));
        nbt.put(NBTConstants.DATA, missileData);
        return nbt;
    }
}
// PORTED
