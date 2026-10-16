package icbm.classic.client.fx;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SmokeParticle;

public class ParticleSmokeICBM extends SmokeParticle
{
    public ParticleSmokeICBM(ClientLevel worldIn, Pos pos, double vx, double vy, double vz, float scale)
    {
        super(worldIn, pos.x(), pos.y(), pos.z(), vx, vy, vz, scale, SmokeSprites.INSTANCE);
    }

    public ParticleSmokeICBM setAge(int age)
    {
        this.lifetime = age;
        return this;
    }

    public ParticleSmokeICBM setColor(float r, float g, float b, boolean addColorVariant)
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
}
// PORTED
