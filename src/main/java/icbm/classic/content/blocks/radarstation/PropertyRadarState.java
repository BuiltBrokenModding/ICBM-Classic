package icbm.classic.content.blocks.radarstation;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PropertyRadarState extends EnumProperty<EnumRadarState> {
    protected PropertyRadarState() {
        super("type", EnumRadarState.class, Lists.newArrayList(EnumRadarState.values()));
    }
}
// PORTED
