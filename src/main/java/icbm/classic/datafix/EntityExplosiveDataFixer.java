package icbm.classic.datafix;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.NBTConstants;
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
            // Move hypersonic to sonic
            if(existingSave.hasKey(NBTConstants.EXPLOSIVE_STACK)) {
                final NBTTagCompound stackSave = existingSave.getCompoundTag(NBTConstants.EXPLOSIVE_STACK);
                if(stackSave.hasKey("Damage")) {
                    final int damage = stackSave.getInteger("Damage");

                    if(damage == ICBMExplosives.HYPERSONIC.getRegistryID()) {

                        // Change to sonic id
                        stackSave.setInteger("Damage", ICBMExplosives.SONIC.getRegistryID());

                        // Wipe out custom data, shouldn't exist but could crash a 3rd-party's code
                        stackSave.removeTag("tag");
                        stackSave.removeTag("ForgeCaps");
                    }
                }
            }
            // Fix explosive ID save
            else if (existingSave.hasKey(EXPLOSIVE_ID))
            {
                final int id = existingSave.getInteger(EXPLOSIVE_ID);

                //Generate stack so we can serialize off it
                final ItemStack stack = new ItemStack(BlockReg.blockExplosive, 1, id);

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
        return 2;
    }
}
