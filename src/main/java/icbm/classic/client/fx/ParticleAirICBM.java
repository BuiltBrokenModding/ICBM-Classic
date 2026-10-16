package icbm.classic.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SmokeParticle;

/**
 * Same as normal smoke, but doesn't move upwards on its own
 */
public class ParticleAirICBM extends SmokeParticle
{
    public ParticleAirICBM(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float scale)
    {
        super(worldIn, x, y, z, vx, vy, vz, scale, SmokeSprites.INSTANCE);
    }

    public ParticleAirICBM setAge(int age)
    {
        this.lifetime = age;
        return this;
    }

    public ParticleAirICBM setColor(float r, float g, float b, boolean addColorVariant)
    {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        if (addColorVariant)
        {
            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            this.rCol *= colorVariant;
            this.bCol *= colorVariant;
            this.gCol *= colorVariant;
        }
        return this;
    }

    @Override
    public void tick() // same code as in vanilla particle, but the vertical velocity acceleration is set to 0
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
            return;
        }

        this.setSpriteFromAge(SmokeSprites.INSTANCE);
        this.move(this.xd, this.yd, this.zd);

        if (this.y == this.yo)
        {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
        }

        this.xd *= 0.95999999D;
        this.yd *= 0.95999999D;
        this.zd *= 0.95999999D;

        if (this.onGround)
        {
            this.xd *= 0.699999988079071D;
            this.zd *= 0.699999988079071D;
        }
    }
}
// PORTED
