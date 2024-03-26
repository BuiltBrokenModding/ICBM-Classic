package icbm.classic.world.block.launcher.frame;

import net.minecraft.util.IStringSerializable;

public enum EnumFrameState implements IStringSerializable {
    TOP,
    MIDDLE,
    BOTTOM;

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
