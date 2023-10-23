package icbm.classic.datafix;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.lib.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class TileRadarStationDataFixer implements IFixableData
{
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound tag)
    {
        if(tag.hasKey(NBTConstants.ID) && tag.getString(NBTConstants.ID).equalsIgnoreCase(ICBMConstants.PREFIX + "radarstation"))
        {
            String firstOldKey = "alarmBanJing";
            String secondOldKey = "safetyBanJing";

            String firstOldKey2 = "alarmRadius";
            String secondOldKey2 = "safetyRadius";

            if(tag.hasKey(firstOldKey) || tag.hasKey(firstOldKey2))
            {
                final int alarmRadius = tag.getInteger(firstOldKey);

                DataFixerHelpers.removeTags(tag, firstOldKey, firstOldKey2);
                tag.setInteger(TileRadarStation.NBT_DETECTION_RANGE, alarmRadius);
            }

            if(tag.hasKey(secondOldKey) | tag.hasKey(secondOldKey2))
            {
                final int safetyRadius = tag.getInteger(secondOldKey);

                DataFixerHelpers.removeTags(tag, secondOldKey, secondOldKey2);
                tag.setInteger(TileRadarStation.NBT_TRIGGER_RANGE, safetyRadius);
            }
        }

        return tag;
    }

    @Override
    public int getFixVersion()
    {
        return ICBMClassic.DATA_FIXER_VERSION;
    }
}
