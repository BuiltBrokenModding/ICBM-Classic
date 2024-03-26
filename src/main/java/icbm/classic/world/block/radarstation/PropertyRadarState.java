package icbm.classic.world.block.radarstation;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;

public class PropertyRadarState extends PropertyEnum<EnumRadarState> {
    protected PropertyRadarState() {
        super("type", EnumRadarState.class, Lists.newArrayList(EnumRadarState.values()));
    }
}
