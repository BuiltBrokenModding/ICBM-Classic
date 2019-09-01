package icbm.classic.client.fx;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Same as normal smoke, but doesn't move upwards on its own
 */
@SideOnly(Side.CLIENT)
public class ParticleAirICBM extends ParticleSmokeNormal
{
    public ParticleAirICBM(World worldIn, Pos pos, double vx, double vy, double vz, float scale)
    {
        super(worldIn, pos.x(), pos.y(), pos.z(), vx, vy, vz, scale);
    }

    public ParticleAirICBM setAge(int age)
    {
        this.particleMaxAge = age;
        return this;
    }

    public ParticleAirICBM setColor(float r, float g, float b, boolean addColorVariant)
    {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;

        if (addColorVariant)
        {
            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            this.particleRed *= colorVariant;
            this.particleBlue *= colorVariant;
            this.particleGreen *= colorVariant;
        }
        return this;
    }

    @Override
    public void onUpdate() // same code as in vanilla particle, but the vertical velocity acceleration is set to 0
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.move(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY)
        {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.95999999D;
        this.motionY *= 0.95999999D;
        this.motionZ *= 0.95999999D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }
}