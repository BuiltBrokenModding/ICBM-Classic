package icbm.classic.prefab.tile;

import com.google.common.collect.Lists;
import icbm.classic.api.EnumTier;
import net.minecraft.block.properties.PropertyEnum;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/31/2018.
 */
public final class PropertyTier extends PropertyEnum<EnumTier>
{
    public PropertyTier()
    {
        super("tier", EnumTier.class, Lists.newArrayList(EnumTier.ONE, EnumTier.TWO, EnumTier.THREE));
    }
}
