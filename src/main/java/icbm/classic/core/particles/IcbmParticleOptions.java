package icbm.classic.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Locale;

public class IcbmParticleOptions extends DustParticleOptionsBase {

    public static final ParticleOptions.Deserializer<IcbmParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        public @NotNull IcbmParticleOptions fromCommand(@NotNull ParticleType<IcbmParticleOptions> type,
                                                        @NotNull StringReader reader) throws CommandSyntaxException {
            Vector3f vector3f = DustParticleOptionsBase.readVector3f(reader);
            reader.expect(' ');
            float scale = reader.readFloat();
            reader.expect(' ');
            int lifetime = reader.readInt();
            return new IcbmParticleOptions(type, vector3f, scale, lifetime);
        }

        public @NotNull IcbmParticleOptions fromNetwork(@NotNull ParticleType<IcbmParticleOptions> type,
                                                        @NotNull FriendlyByteBuf buf) {
            return new IcbmParticleOptions(type, DustParticleOptionsBase.readVector3f(buf), buf.readFloat(), IcbmParticleOptions.readLifetime(buf));
        }
    };

    public static Codec<IcbmParticleOptions> codec(ParticleType<IcbmParticleOptions> type) {
        return RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(options -> options.color),
                    Codec.FLOAT.fieldOf("scale").forGetter(options -> options.scale),
                    Codec.INT.fieldOf("lifetime").forGetter(options -> options.lifetime)
                )
                .apply(instance, (color, scale, lifetime) -> new IcbmParticleOptions(type, color, scale, lifetime))
        );
    }

    private final ParticleType<?> type;
    private final int lifetime;

    public IcbmParticleOptions(ParticleType<?> type, Vector3f pColor, float pScale, int lifetime) {
        super(pColor, pScale);
        this.type = type;
        this.lifetime = lifetime;
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return type;
    }

    protected static int readLifetime(FriendlyByteBuf buffer) {
        return buffer.readVarInt();
    }

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf buffer) {
        super.writeToNetwork(buffer);
        buffer.writeVarInt(this.lifetime);
    }

    @Override
    public @NotNull String writeToString() {
        return String.format(
            Locale.ROOT,
            "%s %d",
            super.toString(),
            this.lifetime
        );
    }

    public int getLifetime() {
        return lifetime;
    }
}
