package icbm.classic.datafix;

import icbm.classic.IcbmConstants;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.world.block.explosive.BlockEntityExplosive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.item.ItemStack;

public class TileExplosivesDataFixer implements IFixableData {
    private static final String TILE_ID = IcbmConstants.PREFIX + "explosive";
    private static final String EXPLOSIVE_ID = "explosiveID";

    @Override
    public CompoundTag fixTagCompound(CompoundTag existingSave) {
        if (existingSave.contains("id") && existingSave.getString("id").equalsIgnoreCase(TILE_ID)) {
            // Migrate old int explosive system to capability
            if (existingSave.contains(EXPLOSIVE_ID)) {

                int explosiveID = existingSave.getInteger(EXPLOSIVE_ID);

                if (explosiveID == 14) //the S-Mine was removed, make it be the default explosive as a fallback
                    explosiveID = ICBMExplosives.CONDENSED.getRegistryID();
                if (explosiveID == ICBMExplosives.HYPERSONIC.getRegistryID()) // hypersonic was removed in 4.3.0
                    explosiveID = ICBMExplosives.SONIC.getRegistryID();
                else if (explosiveID > 14) //since it was removed, all the IDs need to move down by one
                    explosiveID--;

                // Set new data
                existingSave.put(BlockEntityExplosive.NBT_EXPLOSIVE_STACK, new ItemStack(BlockReg.blockExplosive, 1, explosiveID).serializeNBT());

                // Remove old data
                existingSave.remove(EXPLOSIVE_ID);
            }

            // Move hypersonc to sonic
            else if (existingSave.contains(BlockEntityExplosive.NBT_EXPLOSIVE_STACK)) {
                final CompoundTag stackSave = existingSave.getCompound(BlockEntityExplosive.NBT_EXPLOSIVE_STACK);
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

        return existingSave;
    }

    @Override
    public int getFixVersion() {
        return 1;
    }
}
