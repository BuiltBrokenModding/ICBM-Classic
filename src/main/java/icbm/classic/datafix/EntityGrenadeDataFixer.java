package icbm.classic.datafix;

import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.lib.NBTConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;

public class EntityGrenadeDataFixer implements IFixableData {
    @Override
    public CompoundTag fixTagCompound(CompoundTag tag) {
        if (tag.contains(NBTConstants.ID) && tag.getString(NBTConstants.ID).equalsIgnoreCase(ICBMEntities.GRENADE.toString())) {
            String oldKey = "haoMa";

            if (tag.contains(oldKey)) {
                int explosiveID = tag.getInteger(oldKey);

                tag.remove(oldKey); //remove the old entry to not have legacy data. the method name may be misleading, but it actually just removes the key from the tag map
                tag.setInteger(NBTConstants.EXPLOSIVE_ID, explosiveID);
            }
        }

        return tag;
    }

    @Override
    public int getFixVersion() {
        return 1;
    }
}
