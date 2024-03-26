package icbm.classic.client.particle;

import icbm.classic.core.particles.IcbmParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class IcbmSmokeParticle extends SmokeParticle {
    public IcbmSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                             float scale, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, scale, sprites);
    }

    public void setColor(float r, float g, float b, boolean addColorVariant) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        if (addColorVariant) {
            float colorVariant = (float) (Math.random() * 0.9);
            this.rCol *= colorVariant;
            this.bCol *= colorVariant;
            this.gCol *= colorVariant;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<IcbmParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(
            @NotNull IcbmParticleOptions type,
            @NotNull ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            IcbmSmokeParticle particle = new IcbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.getScale(), this.sprites);
            particle.setColor(type.getColor().x(), type.getColor().y(), type.getColor().z(), true);
            particle.setLifetime(type.getLifetime());
            return particle;
        }
    }
}