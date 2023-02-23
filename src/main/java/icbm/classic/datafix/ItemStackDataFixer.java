package icbm.classic.datafix;

import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class ItemStackDataFixer implements IFixableData
{
    private static final String ID = "id";

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound existingSave)
    {
        if (existingSave.hasKey(ID))
        {
            // update hypersonc to sonic
            final String regName = existingSave.getString(ID);
            if(regName.equals(ItemReg.itemExplosiveMissile.getRegistryName().toString())
                || regName.equals(ItemReg.itemBombCart.getRegistryName().toString())
                || regName.equals(BlockReg.blockExplosive.getRegistryName().toString())
            ) {
                fixExSave(existingSave);
            }
        }
        return existingSave;
    }

    private void fixExSave(NBTTagCompound stackSave) {
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

    @Override
    public int getFixVersion()
    {
        return 2;
    }
}
