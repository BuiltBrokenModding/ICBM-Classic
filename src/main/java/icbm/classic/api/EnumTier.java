package icbm.classic.api;

import net.minecraft.util.IStringSerializable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/31/2018.
 */
public enum EnumTier implements IStringSerializable
{
    ONE,
    TWO,
    THREE,
    FOUR;

    @Override
    public String toString()
    {
        return this.getName();
    }

    public String getName()
    {
        return name().toLowerCase();
    }

    public static EnumTier get(int itemDamage)
    {
        if (itemDamage > 0 && itemDamage < values().length)
        {
            return values()[itemDamage];
        }
        return ONE;
    }
}
