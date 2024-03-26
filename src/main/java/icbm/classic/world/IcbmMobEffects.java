package icbm.classic.world;

import icbm.classic.IcbmConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IcbmMobEffects {

    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, IcbmConstants.MOD_ID);
}
