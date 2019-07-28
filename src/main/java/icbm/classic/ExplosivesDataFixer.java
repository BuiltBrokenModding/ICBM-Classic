package icbm.classic;

import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class ExplosivesDataFixer implements IFixableData
{
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound nbt)
    {
        String id = nbt.getString("id");

        if(id.equals("icbmclassic:explosive"))
        {
            NBTTagCompound newNbt = new NBTTagCompound();
            int explosiveID = nbt.getInteger("explosiveID");
            ItemStack stack;

            if(explosiveID == 14) //the S-Mine was removed, make it be the default one
                explosiveID = 0;
            else if(explosiveID > 14) //since it was removed, all the IDs need to move down by one
                explosiveID--;

            stack = new ItemStack(BlockReg.blockExplosive, 1, explosiveID);
            newNbt.setTag(CapabilityExplosiveStack.NBT_STACK, stack.serializeNBT());
            newNbt.setInteger("x", nbt.getInteger("x"));
            newNbt.setInteger("y", nbt.getInteger("y"));
            newNbt.setInteger("z", nbt.getInteger("z"));
            newNbt.setString("id", id);
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
