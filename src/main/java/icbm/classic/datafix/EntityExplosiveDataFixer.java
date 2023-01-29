package icbm.classic.datafix;

import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class EntityExplosiveDataFixer implements IFixableData
{
    private static final String ENTITY_ID = "id";
    private static final String EXPLOSIVE_ID = "explosiveID";
    private static final String DATA = "data";

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound existingSave)
    {
        //Match to entity, we get all entity tags as input
        if (existingSave.hasKey(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMEntities.BLOCK_EXPLOSIVE.toString()))
        {
            //Fix explosive ID save
            if (existingSave.hasKey(EXPLOSIVE_ID))
            {
                final int id = existingSave.getInteger(EXPLOSIVE_ID);

                //Generate stack so we can serialize off it
                final ItemStack stack = new ItemStack(BlockReg.blockExplosive, 1, id);

                //Handle custom explosive data
                final IExplosive ex = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
                if(ex instanceof CapabilityExplosiveStack)
                {
                    if (existingSave.hasKey(DATA))
                    {
                        ((CapabilityExplosiveStack)ex).setCustomData(existingSave.getCompoundTag(DATA));
                    }
                }

                //Remove old tags
                existingSave.removeTag(EXPLOSIVE_ID);
                existingSave.removeTag(DATA);

                //Save stack
                existingSave.setTag(NBTConstants.EXPLOSIVE_STACK, stack.serializeNBT());
            }
        }
        return existingSave;
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
