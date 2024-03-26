package icbm.classic.datafix;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.item.ItemStack;

public class EntityExplosiveDataFixer implements IFixableData {
    private static final String ENTITY_ID = "id";
    private static final String EXPLOSIVE_ID = "explosiveID";
    private static final String DATA = "data";

    @Override
    public CompoundTag fixTagCompound(CompoundTag existingSave) {
        //Match to entity, we get all entity tags as input
        if (existingSave.contains(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMEntities.BLOCK_EXPLOSIVE.toString())) {
            // Move hypersonic to sonic
            if (existingSave.contains(NBTConstants.EXPLOSIVE_STACK)) {
                final CompoundTag stackSave = existingSave.getCompound(NBTConstants.EXPLOSIVE_STACK);
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
            // Fix explosive ID save
            else if (existingSave.contains(EXPLOSIVE_ID)) {
                final int id = existingSave.getInteger(EXPLOSIVE_ID);

                //Generate stack so we can serialize off it
                final ItemStack stack = new ItemStack(BlockReg.blockExplosive, 1, id);

                //Handle custom explosive data
                final IExplosive ex = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
                if (ex instanceof CapabilityExplosiveStack) {
                    if (existingSave.contains(DATA)) {
                        ((CapabilityExplosiveStack) ex).setCustomData(existingSave.getCompound(DATA));
                    }
                }

                //Remove old tags
                existingSave.remove(EXPLOSIVE_ID);
                existingSave.remove(DATA);

                //Save stack
                existingSave.put(NBTConstants.EXPLOSIVE_STACK, stack.serializeNBT());
            }
        }
        return existingSave;
    }

    @Override
    public int getFixVersion() {
        return 2;
    }
}
