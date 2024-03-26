package icbm.classic.world.block.radarstation;

import net.minecraft.util.IStringSerializable;

public enum EnumRadarState implements IStringSerializable {
    ON,
    OFF,
    WARNING,
    DANGER;

    public static EnumRadarState get(int index) {
        if (index >= 0 && index < values().length) {
            return values()[index];
        }
        return OFF;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
