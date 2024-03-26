package icbm.classic.datafix;

import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.IFixableData;

public class EntityBombCartDataFixer implements IFixableData {
    private static final String ENTITY_ID = "id";
    private static final String EXPLOSIVE_ID = "explosive";

    @Override
    public CompoundTag fixTagCompound(CompoundTag existingSave) {
        //Match to entity, we get all entity tags as input
        if (existingSave.contains(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMEntities.BOMB_CART.toString())) {
            // Move hypersonic to sonic
            final int explosiveId = existingSave.getInteger(EXPLOSIVE_ID);
            if (explosiveId == ICBMExplosives.HYPERSONIC.getRegistryID()) {
                existingSave.setInteger(EXPLOSIVE_ID, ICBMExplosives.SONIC.getRegistryID());
            }
        }
        return existingSave;
    }

    @Override
    public int getFixVersion() {
        return 2;
    }
}
