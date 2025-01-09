package icbm.classic.content.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.Vec3d;

/**
 * @deprecated remove at some point and apply potion effects directly
 */
@Deprecated
public class ContagiousPoison extends Poison
{
    private final boolean isContagious;

    public ContagiousPoison(String name, int id, boolean isContagious)
    {
        super(name);
        this.isContagious = isContagious;
    }

    @Override
    protected void doPoisonEntity(Vec3d emitPosition, LivingEntity entity, int amplifier)
    {
        if (this.isContagious)
        {
            entity.addPotionEffect(new CustomPotionEffect(Effects.POISON, 45 * 20, amplifier, null)); //TODO was contagious potion
            entity.addPotionEffect(new CustomPotionEffect(Effects.BLINDNESS, 15 * 20, amplifier));
        }
        else
        {
            entity.addPotionEffect(new CustomPotionEffect(Effects.POISON, 30 * 20, amplifier, null)); //TODO was toxin potion
            entity.addPotionEffect(new CustomPotionEffect(Effects.NAUSEA, 30 * 20, amplifier));
        }

        entity.addPotionEffect(new CustomPotionEffect(Effects.HUNGER, 30 * 20, amplifier));
        entity.addPotionEffect(new CustomPotionEffect(Effects.WEAKNESS, 35 * 20, amplifier));
        entity.addPotionEffect(new CustomPotionEffect(Effects.MINING_FATIGUE, 60 * 20, amplifier));
    }
}
