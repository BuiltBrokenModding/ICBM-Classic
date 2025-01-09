package icbm.classic.client;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.config.ConfigClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientProxy
{



    public void spawnSmoke(World world, Vec3d position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world != null)
        {

            Particle particle = Minecraft.getInstance().particles.addParticle(ParticleTypes.SMOKE, position.x, position.y, position.z, v, v1, v2);
            particle.setMaxAge(ticksToLive);

            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            particle.setColor(red * colorVariant, green * colorVariant, blue * colorVariant);
            particle.multipleParticleScaleBy(scale);
        }
    }


    public void spawnAirParticle(World world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world != null)
        {
            Particle particle = Minecraft.getInstance().particles.addParticle(ParticleTypes.SMOKE, x, y, z, v, v1, v2);
            particle.setMaxAge(ticksToLive);

            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            particle.setColor(red * colorVariant, green * colorVariant, blue * colorVariant);
            particle.multipleParticleScaleBy(scale);
        }
    }


    public void spawnExplosionParticles(final World world, final double sourceX, final double sourceY, final double sourceZ, final double blastScale, final BlockPos blockPos)
    {
        //Random position near destroyed block
        final double particleX = (blockPos.getX() + world.rand.nextFloat());
        final double particleY = (blockPos.getY() + world.rand.nextFloat());
        final double particleZ = (blockPos.getZ() + world.rand.nextFloat());

        //Get delta from center of blast so particles move out
        double particleMX = particleX - sourceX;
        double particleMY = particleY - sourceY;
        double particleMZ = particleZ - sourceZ;

        //Normalize motion vector
        final double speed = MathHelper.sqrt(particleMX * particleMX + particleMY * particleMY + particleMZ * particleMZ);
        particleMX /= speed;
        particleMY /= speed;
        particleMZ /= speed;

        //Give motion vector a randomized multiplier based on blast size
        double multiplier = 0.5D / (speed / blastScale + 0.1D);
        multiplier *= (world.rand.nextFloat() * world.rand.nextFloat() + 0.3F);
        particleMX *= multiplier;
        particleMY *= multiplier;
        particleMZ *= multiplier;

        world.addParticle(ParticleTypes.EXPLOSION,
            (particleX + sourceX) / 2.0D,
            (particleY + sourceY) / 2.0D,
            (particleZ + sourceZ) / 2.0D,
            particleMX, particleMY, particleMZ);
        world.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, particleMX, particleMY, particleMZ);
    }


    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if (entity.world.isRemote && ConfigClient.MISSILE_ENGINE_SMOKE)
        {
            final double smokeVelocityScale = 0.5f;
            final double smokeRandomPercent = 0.025;
            final double distance = -1.2f;
            final int particleLifeSpan = 100;

            final float particleScale =(int) Math.max(1d, 6d * (1 / (1 + entity.posY / 100)));

            final double dH = Math.cos(Math.toRadians(entity.rotationPitch)) * distance;

            double x = Math.sin(Math.toRadians(entity.rotationYaw)) * dH;
            double y = Math.sin(Math.toRadians(entity.rotationPitch)) * distance;
            double z = Math.cos(Math.toRadians(entity.rotationYaw)) * dH;

            for (int i = 0; i < 10; i++)
            {
                spawnAirParticle(entity.world,

                    // Position
                    entity.posX + x,
                    entity.posY + y,
                    entity.posZ + z,

                    // Motion inverse of missile path
                    -entity.getMotion().x * smokeVelocityScale,
                    -entity.getMotion().y * smokeVelocityScale,
                    -entity.getMotion().z * smokeVelocityScale,

                    // Color
                    flightLogic.engineSmokeRed(entity),
                    flightLogic.engineSmokeGreen(entity),
                    flightLogic.engineSmokeBlue(entity),

                    particleScale, particleLifeSpan);

                x *= 1 - smokeRandomPercent * Math.random();
                y *= 1 - smokeRandomPercent * Math.random();
                z *= 1 - smokeRandomPercent * Math.random();
            }
        }
    }


    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if(ConfigClient.MISSILE_LAUNCH_SMOKE) {
            final World world = entity.world;
            final Random random = world.rand;

            double posX = entity.posX;
            double posY = entity.posY - 1.2; //TODO get missile height from type
            double posZ = entity.posZ;

            //Spawn smoke TODO add config for smoke amount
            for (int smokeCount = 0; smokeCount < 10; smokeCount++) {
                //Randomize flight direction down in a cone
                final double velX = (random.nextFloat() - random.nextFloat()) * 0.3;
                final double velY = 1 - (random.nextFloat() * 0.5);
                final double velZ = (random.nextFloat() - random.nextFloat()) * 0.3;

                //spawn smoke
                Particle particle = Minecraft.getInstance().particles.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, velX, velY, velZ);
                particle.setMaxAge(180); //TODO config

                float colorVariant = (float) (Math.random() * 0.90000001192092896D);
                particle.setColor(colorVariant, colorVariant, colorVariant);
                particle.multipleParticleScaleBy(2 * random.nextFloat());
            }
        }
    }
}
