package icbm.classic.content.blocks.radarstation;

import net.minecraft.util.IStringSerializable;

public enum EnumRadarState implements IStringSerializable {
    ON,
    OFF,
    WARNING,
    DANGER;

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
