package icbm.classic.api;

import net.minecraft.util.IStringSerializable;

/**
 *
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
