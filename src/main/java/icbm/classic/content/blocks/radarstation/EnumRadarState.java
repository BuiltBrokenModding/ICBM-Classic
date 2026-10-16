package icbm.classic.content.blocks.radarstation;

import net.minecraft.util.StringRepresentable;

public enum EnumRadarState implements StringRepresentable {
    ON,
    OFF,
    WARNING,
    DANGER;

    public static EnumRadarState get(int index) {
        if(index >= 0 && index < values().length) {
            return values()[index];
        }
        return OFF;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    /** 1.12 name */
    public String getName() {
        return getSerializedName();
    }
}
// PORTED
