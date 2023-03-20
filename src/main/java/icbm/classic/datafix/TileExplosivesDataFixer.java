package icbm.classic.datafix;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.lib.NBTConstants;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class TileExplosivesDataFixer implements IFixableData
{
    private static final String TILE_ID = ICBMConstants.PREFIX + "explosive";
    private static final String EXPLOSIVE_ID = "explosiveID";
    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound existingSave)
    {
        if (existingSave.hasKey("id") && existingSave.getString("id").equalsIgnoreCase(TILE_ID))
        {
            // Migrate old int explosive system to capability
            if(existingSave.hasKey(EXPLOSIVE_ID)) {

                int explosiveID = existingSave.getInteger(EXPLOSIVE_ID);

                if (explosiveID == 14) //the S-Mine was removed, make it be the default explosive as a fallback
                    explosiveID = ICBMExplosives.CONDENSED.getRegistryID();
                if (explosiveID == ICBMExplosives.HYPERSONIC.getRegistryID()) // hypersonic was removed in 4.3.0
                    explosiveID = ICBMExplosives.SONIC.getRegistryID();
                else if (explosiveID > 14) //since it was removed, all the IDs need to move down by one
                    explosiveID--;

                // Set new data
                existingSave.setTag(TileEntityExplosive.NBT_EXPLOSIVE_STACK, new ItemStack(BlockReg.blockExplosive, 1, explosiveID).serializeNBT());

                // Remove old data
                existingSave.removeTag(EXPLOSIVE_ID);
            }

            // Move hypersonc to sonic
            else if(existingSave.hasKey(TileEntityExplosive.NBT_EXPLOSIVE_STACK)) {
                final NBTTagCompound stackSave = existingSave.getCompoundTag(TileEntityExplosive.NBT_EXPLOSIVE_STACK);
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

        return existingSave;
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
