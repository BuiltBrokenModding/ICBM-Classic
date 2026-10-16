package icbm.classic.content.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Base for ICBM mob effects.
 * <p>
 * PORT: 1.12 registered the potion directly into the registry from the constructor with the name
 * {@code icbmclassic:<name>} and translation key {@code potion.<name>}. Registration now goes through
 * {@code ICBMClassic.EFFECTS}; the id and name arguments are kept for compatibility only.
 */
public abstract class CustomPotion extends MobEffect
{
    protected final String potionName;

    /**
     * Creates a new type of potion
     *
     * @param isBadEffect - Is this potion a good potion or a bad one?
     * @param color       - The color of this potion.
     * @param name        - The name of this potion.
     */
    public CustomPotion(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
        this.potionName = "potion." + name;
    }
}
// PORTED
