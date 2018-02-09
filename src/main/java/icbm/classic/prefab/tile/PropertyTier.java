package icbm.classic.prefab.tile;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/31/2018.
 */
public final class PropertyTier extends PropertyEnum<EnumTier>
{
    public PropertyTier()
    {
        super("tier", EnumTier.class, Lists.newArrayList(EnumTier.ONE, EnumTier.TWO, EnumTier.THREE));
    }
}
