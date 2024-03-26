package icbm.classic.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum WeaponTier implements StringRepresentable {
    NONE(Style.EMPTY.applyFormat(ChatFormatting.WHITE)),
    ONE(Style.EMPTY.applyFormat(ChatFormatting.GREEN)),
    TWO(Style.EMPTY.applyFormat(ChatFormatting.YELLOW)),
    THREE(Style.EMPTY.applyFormat(ChatFormatting.GOLD)),
    FOUR(Style.EMPTY.applyFormat(ChatFormatting.RED));

    private static final WeaponTier[] VALUES = values();
    private final Style tooltipStyle;

    WeaponTier(Style tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
    }

    public Style getTooltipStyle() {
        return tooltipStyle;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    public Component getLocalizedName() {
        return Component.translatable("tier.icbmclassic." + getSerializedName());
    }

    public static WeaponTier get(int tier) {
        if (tier >= 0 && tier < values().length) {
            return VALUES[tier];
        }
        return ONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
