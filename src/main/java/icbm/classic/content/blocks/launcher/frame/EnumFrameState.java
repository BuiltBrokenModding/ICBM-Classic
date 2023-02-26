package icbm.classic.content.blocks.launcher.frame;

import net.minecraft.util.IStringSerializable;

public enum EnumFrameState implements IStringSerializable {
    TOP,
    MIDDLE,
    BOTTOM,
    DEFAULT;

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
