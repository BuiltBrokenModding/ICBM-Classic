package icbm.classic.datafix;

import icbm.classic.api.EntityRefs;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class EntityExplosiveDataFixer implements IFixableData
{
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        //Match to entity, we get all entity tags as input
        if (compound.hasKey(NBTConstants.ID) && compound.getString(NBTConstants.ID).equalsIgnoreCase(EntityRefs.BLOCK_EXPLOSIVE.toString()))
        {
            //Fix explosive ID save
            if (compound.hasKey(NBTConstants.EXPLOSIVE_ID))
            {
                int id = compound.getInteger(NBTConstants.EXPLOSIVE_ID);

                //Generate stack so we can serialize off it
                final ItemStack stack = new ItemStack(BlockReg.blockExplosive, 1, id);

                //Handle custom explosive data
                final IExplosive ex = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
                if(ex instanceof CapabilityExplosiveStack)
                {
                    if (compound.hasKey(NBTConstants.DATA))
                    {
                        ((CapabilityExplosiveStack)ex).setCustomData(compound.getCompoundTag(NBTConstants.DATA));
                    }
                }

                //Remove old tags
                compound.removeTag(NBTConstants.EXPLOSIVE_ID);
                compound.removeTag(NBTConstants.DATA);

                //Save stack
                compound.setTag(NBTConstants.EXPLOSIVE_STACK, stack.serializeNBT());
            }
        }
        return compound;
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
