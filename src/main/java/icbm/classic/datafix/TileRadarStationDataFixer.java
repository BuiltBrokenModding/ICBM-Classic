package icbm.classic.datafix;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.lib.NBTConstants;
import icbm.classic.world.block.radarstation.TileRadarStation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;

public class TileRadarStationDataFixer implements IFixableData {
    @Override
    public CompoundTag fixTagCompound(CompoundTag tag) {
        if (tag.contains(NBTConstants.ID) && tag.getString(NBTConstants.ID).equalsIgnoreCase(IcbmConstants.PREFIX + "radarstation")) {
            String firstOldKey = "alarmBanJing";
            String secondOldKey = "safetyBanJing";

            String firstOldKey2 = "alarmRadius";
            String secondOldKey2 = "safetyRadius";

            if (tag.contains(firstOldKey) || tag.contains(firstOldKey2)) {
                final int alarmRadius = tag.getInteger(firstOldKey);

                DataFixerHelpers.removeTags(tag, firstOldKey, firstOldKey2);
                tag.setInteger(TileRadarStation.NBT_DETECTION_RANGE, alarmRadius);
            }

            if (tag.contains(secondOldKey) | tag.contains(secondOldKey2)) {
                final int safetyRadius = tag.getInteger(secondOldKey);

                DataFixerHelpers.removeTags(tag, secondOldKey, secondOldKey2);
                tag.setInteger(TileRadarStation.NBT_TRIGGER_RANGE, safetyRadius);
            }
        }

        return tag;
    }

    @Override
    public int getFixVersion() {
        return ICBMClassic.DATA_FIXER_VERSION;
    }
}
