package icbm.classic.datafix;

import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.world.IcbmItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;

public class ItemStackDataFixer implements IFixableData {
    private static final String ID = "id";

    @Override
    public CompoundTag fixTagCompound(CompoundTag existingSave) {
        if (existingSave.contains(ID)) {
            // update hypersonc to sonic
            final String regName = existingSave.getString(ID);
            if (regName.equals(IcbmItems.itemExplosiveMissile.getRegistryName().toString())
                || regName.equals(IcbmItems.itemBombCart.getRegistryName().toString())
                || regName.equals(BlockReg.blockExplosive.getRegistryName().toString())
            ) {
                fixExSave(existingSave);
            }
        }
        return existingSave;
    }

    private void fixExSave(CompoundTag stackSave) {
        if (stackSave.contains("Damage")) {
            final int damage = stackSave.getInteger("Damage");

            if (damage == ICBMExplosives.HYPERSONIC.getRegistryID()) {

                // Change to sonic id
                stackSave.setInteger("Damage", ICBMExplosives.SONIC.getRegistryID());

                // Wipe out custom data, shouldn't exist but could crash a 3rd-party's code
                stackSave.remove("tag");
                stackSave.remove("ForgeCaps");
            }
        }
    }

    @Override
    public int getFixVersion() {
        return 2;
    }
}
