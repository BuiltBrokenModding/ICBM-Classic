package icbm.classic.client.particle;

import icbm.classic.core.particles.IcbmParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class AntimatterParticle extends TextureSheetParticle {

    public AntimatterParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed, float scale) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        this.xd += xSpeed;
        this.yd += ySpeed;
        this.zd += zSpeed;
        this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.3);
        this.scale(0.75F * scale);
        this.lifetime = (int) (10D / (Math.random() * 0.8D + 0.2D));
        this.lifetime = (int) (this.lifetime * scale);
    }

    public int getBrightnessForRender(float p_189214_1_) {
        return 240;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
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
            AntimatterParticle particle = new AntimatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.getScale());
            particle.pickSprite(this.sprites);
            particle.setLifetime(type.getLifetime());
            return particle;
        }
    }
}
