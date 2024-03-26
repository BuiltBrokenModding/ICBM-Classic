package icbm.classic.world;

import icbm.classic.IcbmConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IcbmPotions {

    public static final DeferredRegister<Potion> REGISTER = DeferredRegister.create(Registries.POTION, IcbmConstants.MOD_ID);

    public static final DeferredHolder<Potion, Potion> CHEMICAL_POISON = REGISTER.register("chemical_poison",
        resourceLocation -> new Potion(resourceLocation.getPath(),
            new MobEffectInstance(MobEffects.POISON, 45 * 20),
            new MobEffectInstance(MobEffects.CONFUSION, 35 * 20),
            new MobEffectInstance(MobEffects.HUNGER, 30 * 20),
            new MobEffectInstance(MobEffects.WEAKNESS, 35 * 20),
            new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 * 20)
        ));

    public static final DeferredHolder<Potion, Potion> CONTAGIOUS_POISON = REGISTER.register("contagious_poison",
        resourceLocation -> new Potion(resourceLocation.getPath(),
            new MobEffectInstance(MobEffects.POISON, 45 * 20),
            new MobEffectInstance(MobEffects.BLINDNESS, 15 * 20),
            new MobEffectInstance(MobEffects.HUNGER, 30 * 20),
            new MobEffectInstance(MobEffects.WEAKNESS, 35 * 20),
            new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 * 20)
        ));


}
