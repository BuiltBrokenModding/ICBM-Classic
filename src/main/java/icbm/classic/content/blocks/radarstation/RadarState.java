package icbm.classic.content.blocks.radarstation;

import net.minecraft.util.IStringSerializable;

public enum RadarState implements IStringSerializable {
    ON("on"),
    OFF("off"),
    WARNING("warning"),
    DANGER("danger");

    private final String name;

    RadarState(String name) {
        this.name = name;
    }

    public static RadarState get(int index) {
        if(index >= 0 && index < values().length) {
            return values()[index];
        }
        return OFF;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
