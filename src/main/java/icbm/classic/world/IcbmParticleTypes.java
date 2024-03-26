package icbm.classic.world;

import com.mojang.serialization.Codec;
import icbm.classic.IcbmConstants;
import icbm.classic.core.particles.IcbmParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class IcbmParticleTypes {

    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, IcbmConstants.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<IcbmParticleOptions>> STALE_SMOKE = register(
        "stale_smoke", false, IcbmParticleOptions.DESERIALIZER, IcbmParticleOptions::codec);

    public static final DeferredHolder<ParticleType<?>, ParticleType<IcbmParticleOptions>> ANTIMATTER = register(
        "antimatter", false, IcbmParticleOptions.DESERIALIZER, IcbmParticleOptions::codec);

    public static final DeferredHolder<ParticleType<?>, ParticleType<IcbmParticleOptions>> LAUNCHER_SMOKE = register(
        "launcher_smoke", false, IcbmParticleOptions.DESERIALIZER, IcbmParticleOptions::codec);

    public static final DeferredHolder<ParticleType<?>, ParticleType<IcbmParticleOptions>> ICBM_SMOKE = register(
        "icbm_smoke", false, IcbmParticleOptions.DESERIALIZER, IcbmParticleOptions::codec);

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(
        String key,
        boolean overrideLimiter,
        ParticleOptions.Deserializer<T> deserializer,
        Function<ParticleType<T>, Codec<T>> codecFactory
    ) {
        return REGISTER.register(key, () -> new ParticleType<>(overrideLimiter, deserializer) {
            @Override
            public Codec<T> codec() {
                return codecFactory.apply(this);
            }
        });
    }
}
