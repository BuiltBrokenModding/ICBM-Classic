package icbm.classic.content.potion;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * @deprecated remove at some point and apply potion effects directly
 */
@Deprecated
public class ContagiousPoison extends Poison
{
    /** Same colors as the chemical (0.8, 0.8, 0) and contagious (0.3, 0.8, 0) gas particles */
    public static final int CHEMICAL_COLOR = 0xCCCC00;
    public static final int CONTAGIOUS_COLOR = 0x4DCC00;

    private final boolean isContagious;

    public ContagiousPoison(String name, int id, boolean isContagious)
    {
        super(name, isContagious ? CONTAGIOUS_COLOR : CHEMICAL_COLOR);
        this.isContagious = isContagious;
    }

    @Override
    protected void doPoisonEntity(Pos emitPosition, LivingEntity entity, int amplifier)
    {
        if (this.isContagious)
        {
            entity.addEffect(new CustomPotionEffect(MobEffects.POISON, 45 * 20, amplifier, null)); //TODO was contagious potion
            entity.addEffect(new CustomPotionEffect(MobEffects.BLINDNESS, 15 * 20, amplifier));
        }
        else
        {
            entity.addEffect(new CustomPotionEffect(MobEffects.POISON, 30 * 20, amplifier, null)); //TODO was toxin potion
            entity.addEffect(new CustomPotionEffect(MobEffects.CONFUSION, 30 * 20, amplifier));
        }

        entity.addEffect(new CustomPotionEffect(MobEffects.HUNGER, 30 * 20, amplifier));
        entity.addEffect(new CustomPotionEffect(MobEffects.WEAKNESS, 35 * 20, amplifier));
        entity.addEffect(new CustomPotionEffect(MobEffects.DIG_SLOWDOWN, 60 * 20, amplifier));
    }
}
// PORTED
