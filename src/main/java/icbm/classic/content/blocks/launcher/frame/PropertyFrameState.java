package icbm.classic.content.blocks.launcher.frame;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;

public class PropertyFrameState extends PropertyEnum<EnumFrameState> {
    protected PropertyFrameState() {
        super("type", EnumFrameState.class, Lists.newArrayList(EnumFrameState.values()));
    }
}
