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

/**
 * Same as normal smoke, but doesn't move upwards on its own
 */
@OnlyIn(Dist.CLIENT)
public class StaleSmokeParticle extends SmokeParticle {
    private final SpriteSet sprites;

    public StaleSmokeParticle(ClientLevel levelIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                              float scale, SpriteSet spriteSet) {
        super(levelIn, x, y, z, xSpeed, ySpeed, zSpeed, scale, spriteSet);
        this.sprites = spriteSet;
    }

    public void setColor(float r, float g, float b, boolean addColorVariant) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        if (addColorVariant) {
            float colorVariant = (float) (Math.random() * 0.9);
            this.rCol *= colorVariant;
            this.gCol *= colorVariant;
            this.bCol *= colorVariant;
        }
    }

    @Override
    public void tick() { // same code as in vanilla particle, but the vertical velocity acceleration is set to 0
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
//            this.yd -= 0.04 * (double) this.gravity;
            this.setSpriteFromAge(this.sprites);
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
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
            StaleSmokeParticle particle = new StaleSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.getScale(),
                this.sprites);
            particle.setColor(type.getColor().x(), type.getColor().y(), type.getColor().z(), false);
            particle.setLifetime(type.getLifetime());
            return particle;
        }
    }
}