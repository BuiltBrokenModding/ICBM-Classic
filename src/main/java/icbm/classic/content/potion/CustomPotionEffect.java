package icbm.classic.content.potion;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CustomPotionEffect extends MobEffectInstance
{

    public CustomPotionEffect(Holder<MobEffect> potion, int duration, int amplifier)
    {
        super(potion, duration, amplifier);
    }

    /** Creates a potion effect with custom curable items.
     *
     * @param curativeItems - ItemStacks that can cure this potion effect, null or empty for none */
    public CustomPotionEffect(Holder<MobEffect> potionID, int duration, int amplifier, List<ItemStack> curativeItems)
    {
        super(potionID, duration, amplifier);

        // PORT: 1.12 curative items became NeoForge EffectCures. Every ICBM caller passes null (no cure),
        // so all cures (milk, honey...) are removed. A non-empty item list can't be mapped and is ignored.
        this.getCures().clear(); // PORT-CHECK: NeoForge 21.1 MobEffectInstance#getCures() returns the mutable cure set
    }
}
// PORTED
