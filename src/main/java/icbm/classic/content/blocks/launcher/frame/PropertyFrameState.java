package icbm.classic.content.blocks.launcher.frame;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PropertyFrameState extends EnumProperty<EnumFrameState> {
    protected PropertyFrameState() {
        super("type", EnumFrameState.class, Lists.newArrayList(EnumFrameState.values()));
    }
}
// PORTED
