package icbm.classic.world.block.emptower;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class PropertyTowerStates extends PropertyEnum<PropertyTowerStates.EnumTowerTypes> {
    protected PropertyTowerStates() {
        super("type", EnumTowerTypes.class, Lists.newArrayList(EnumTowerTypes.values()));
    }

    public static enum EnumTowerTypes implements IStringSerializable {
        BASE,
        COIL,
        ELECTRIC,
        SPIN;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }
}