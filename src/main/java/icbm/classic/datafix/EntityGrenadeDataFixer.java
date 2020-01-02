package icbm.classic.datafix;

import icbm.classic.api.EntityRefs;
import icbm.classic.api.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class EntityGrenadeDataFixer implements IFixableData
{
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound tag)
    {
        if(tag.hasKey(NBTConstants.ID) && tag.getString(NBTConstants.ID).equalsIgnoreCase(EntityRefs.GRENADE.toString()))
        {
            String oldKey = "haoMa";

            if(tag.hasKey(oldKey))
            {
                int explosiveID = tag.getInteger(oldKey);

                tag.removeTag(oldKey); //remove the old entry to not have legacy data. the method name may be misleading, but it actually just removes the key from the tag map
                tag.setInteger(NBTConstants.EXPLOSIVE_ID, explosiveID);
            }
        }

        return tag;
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
