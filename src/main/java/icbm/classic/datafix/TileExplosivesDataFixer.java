package icbm.classic.datafix;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.NBTConstants;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class TileExplosivesDataFixer implements IFixableData
{
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound nbt)
    {
        String id = nbt.getString(NBTConstants.ID);

        if(id.equals(ICBMConstants.PREFIX + "explosive"))
        {
            NBTTagCompound newNbt = new NBTTagCompound();
            int explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);

            if(explosiveID == 14) //the S-Mine was removed, make it be the default explosive as a fallback
                explosiveID = 0;
            else if(explosiveID > 14) //since it was removed, all the IDs need to move down by one
                explosiveID--;

            newNbt.setTag(NBTConstants.EXPLOSIVE_STACK, new ItemStack(BlockReg.blockExplosive, 1, explosiveID).serializeNBT());
            newNbt.setInteger(NBTConstants.X, nbt.getInteger(NBTConstants.X));
            newNbt.setInteger(NBTConstants.Y, nbt.getInteger(NBTConstants.Y));
            newNbt.setInteger(NBTConstants.Z, nbt.getInteger(NBTConstants.Z));
            newNbt.setString(NBTConstants.ID, id);
            return newNbt;
        }

        return nbt;
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
