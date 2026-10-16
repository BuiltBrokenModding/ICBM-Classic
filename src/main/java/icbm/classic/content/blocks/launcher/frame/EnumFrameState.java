package icbm.classic.content.blocks.launcher.frame;

import net.minecraft.util.StringRepresentable;

public enum EnumFrameState implements StringRepresentable {
    TOP,
    MIDDLE,
    BOTTOM;

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
