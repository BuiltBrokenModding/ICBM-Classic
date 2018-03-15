package icbm.classic.client.fx;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSmokeICBM extends Particle
{
    float smokeParticleScale;

    public ParticleSmokeICBM(World worldIn, Pos pos, double vx, double vy, double vz, float scale)
    {
        super(worldIn, pos.x(), pos.y(), pos.z(), 0.0D, 0.0D, 0.0D);

        //Set motion
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += vx;
        this.motionY += vy;
        this.motionZ += vz;

        //Set color
        float f = (float) (Math.random() * 0.30000001192092896D);
        this.particleRed = f;
        this.particleGreen = f;
        this.particleBlue = f;

        //Set scale
        this.particleScale *= 0.75F;
        this.particleScale *= scale;
        this.smokeParticleScale = this.particleScale;

        //Set age
        this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        this.particleMaxAge = (int) ((float) this.particleMaxAge * scale);
    }

    public ParticleSmokeICBM setAge(int age)
    {
        this.particleMaxAge = age;
        return this;
    }

    public ParticleSmokeICBM setColor(float r, float g, float b)
    {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        return this;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float f = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge * 32.0F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        this.particleScale = this.smokeParticleScale * f;
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.motionY += 0.004D;
        this.move(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY)
        {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }
}