package icbm.classic.world.effect;

import net.minecraft.potion.Potion;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomPotionEffect extends MobEffect {

    public CustomPotionEffect(Potion potion, int duration, int amplifier) {
        super(potion, duration, amplifier);
    }

    /**
     * Creates a potion effect with custom curable items.
     *
     * @param curativeItems - ItemStacks that can cure this potion effect
     */
    public CustomPotionEffect(Potion potionID, int duration, int amplifier, List<ItemStack> curativeItems) {
        super(potionID, duration, amplifier);

        if (curativeItems == null) {
            this.setCurativeItems(new ArrayList<ItemStack>());
        } else {
            this.setCurativeItems(curativeItems);
        }
    }
}
