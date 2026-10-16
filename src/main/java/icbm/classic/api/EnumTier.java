package icbm.classic.api;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.util.StringRepresentable;
import net.minecraft.ChatFormatting;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/31/2018.
 */
public enum EnumTier implements StringRepresentable
{
    ONE(ChatFormatting.GREEN),
    TWO(ChatFormatting.YELLOW),
    THREE(ChatFormatting.GOLD),
    FOUR(ChatFormatting.RED),
    NONE(ChatFormatting.WHITE); //TODO we should move the colors to the translations directly

    private ChatFormatting tooltipColor;

    EnumTier(ChatFormatting tooltipColor)
    {
        this.tooltipColor = tooltipColor;
    }

    public ChatFormatting getTooltipColor()
    {
        return tooltipColor;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }

    public String getLocalizedName()
    {
        return LanguageUtility.getLocal("tier.icbmclassic." + getName());
    }

    public String getName()
    {
        return name().toLowerCase();
    }

    @Override
    public String getSerializedName()
    {
        return getName();
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
// PORTED
