package icbm.classic.content.missile.tracker;

import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

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

    public EntityType<?> entityType;
    public CompoundNBT missileData;  //Additional missile data

    //Constructors
    public MissileTrackerData(EntityExplosiveMissile missile)
    {
        targetPos = new Pos(missile.getMissileCapability().getTargetData().getPosition()); //TODO switch to storing targeting data
        missileData = new CompoundNBT();
        entityType = missile.getType();
        missile.writeWithoutTypeId(missileData);
        missileData.remove("Pos");
    }

    public MissileTrackerData(CompoundNBT tagCompound)
    {
        readFromNBT(tagCompound);
    }

    //Helper methods for saving and loading
    public void readFromNBT(CompoundNBT nbt)
    {
        entityType = EntityType.byKey(nbt.getString("entity_type")).orElse(EntityReg.MISSILE_CONDENSED.get()); //TODO handle better
        ticksLeftToTarget = nbt.getInt(NBTConstants.TICKS);
        targetPos = new Pos(nbt.getCompound(NBTConstants.TARGET));

        missileData = nbt.getCompound(NBTConstants.DATA);
    }

    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        nbt.putString("entity_type", entityType.getRegistryName().toString());
        nbt.putInt(NBTConstants.TICKS, ticksLeftToTarget);
        nbt.put(NBTConstants.TARGET, targetPos.writeNBT(new CompoundNBT()));
        nbt.put(NBTConstants.DATA, missileData);
        return nbt;
    }
}
