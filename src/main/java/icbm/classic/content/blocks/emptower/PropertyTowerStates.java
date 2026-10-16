package icbm.classic.content.blocks.emptower;

import com.google.common.collect.Lists;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PropertyTowerStates extends EnumProperty<PropertyTowerStates.EnumTowerTypes> {
    protected PropertyTowerStates() {
        super("type", EnumTowerTypes.class, Lists.newArrayList(EnumTowerTypes.values()));
    }

    public static enum EnumTowerTypes implements StringRepresentable {
        BASE,
        COIL,
        ELECTRIC,
        SPIN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        /** 1.12 name */
        public String getName() {
            return getSerializedName();
        }
    }
}
// PORTED
