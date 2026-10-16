package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.client.fx.ParticleAirICBM;
import icbm.classic.client.fx.ParticleLauncherSmoke;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.config.ConfigClient;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;

/**
 * Client side proxy, created reflectively by {@link icbm.classic.ICBMClassic} on the physical client.
 * <p>
 * PORT: the chicken helmet layer moved from init() to {@link ClientReg} (EntityRenderersEvent.AddLayers).
 * Particles are still created directly and handed to the particle engine like 1.12.
 */
public class ClientProxy extends CommonProxy
{
    public ClientProxy()
    {
    }

    @Override
    public void preInit(IEventBus modBus)
    {
        super.preInit(modBus);
        ClientReg.register(modBus);
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public boolean isShiftHeld()
    {
        return Screen.hasShiftDown();
    }

    @Override
    public void spawnSmoke(Level world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world instanceof ClientLevel clientLevel)
        {
            ParticleSmokeICBM particleSmokeICBM = new ParticleSmokeICBM(clientLevel, position, v, v1, v2, scale);
            particleSmokeICBM.setColor(red, green, blue, true);
            particleSmokeICBM.setAge(ticksToLive);
            Minecraft.getInstance().particleEngine.add(particleSmokeICBM);
        }
    }

    @Override
    public void spawnAirParticle(Level world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world instanceof ClientLevel clientLevel)
        {
            ParticleAirICBM particleAirParticleICBM = new ParticleAirICBM(clientLevel, x, y, z, v, v1, v2, scale);
            particleAirParticleICBM.setColor(red, green, blue, true);
            particleAirParticleICBM.setAge(ticksToLive);
            Minecraft.getInstance().particleEngine.add(particleAirParticleICBM);
        }
        else if (world != null && !world.isClientSide())
        {
            // Integrated server thread, send to clients like the common proxy
            super.spawnAirParticle(world, x, y, z, v, v1, v2, red, green, blue, scale, ticksToLive);
        }
    }

    @Override
    public void spawnExplosionParticles(final Level world, final double sourceX, final double sourceY, final double sourceZ, final double blastScale, final BlockPos blockPos)
    {
        if (!(world instanceof ClientLevel))
        {
            // Integrated server thread, send to clients like the common proxy
            super.spawnExplosionParticles(world, sourceX, sourceY, sourceZ, blastScale, blockPos);
            return;
        }

        final RandomSource rand = world.random;

        //Random position near destroyed block
        final double particleX = (blockPos.getX() + rand.nextFloat());
        final double particleY = (blockPos.getY() + rand.nextFloat());
        final double particleZ = (blockPos.getZ() + rand.nextFloat());

        //Get delta from center of blast so particles move out
        double particleMX = particleX - sourceX;
        double particleMY = particleY - sourceY;
        double particleMZ = particleZ - sourceZ;

        //Normalize motion vector
        final double speed = Math.sqrt(particleMX * particleMX + particleMY * particleMY + particleMZ * particleMZ);
        particleMX /= speed;
        particleMY /= speed;
        particleMZ /= speed;

        //Give motion vector a randomized multiplier based on blast size
        double multiplier = 0.5D / (speed / blastScale + 0.1D);
        multiplier *= (rand.nextFloat() * rand.nextFloat() + 0.3F);
        particleMX *= multiplier;
        particleMY *= multiplier;
        particleMZ *= multiplier;

        // 1.12 EXPLOSION_NORMAL is POOF, SMOKE_NORMAL is SMOKE
        world.addParticle(ParticleTypes.POOF,
            (particleX + sourceX) / 2.0D,
            (particleY + sourceY) / 2.0D,
            (particleZ + sourceZ) / 2.0D,
            particleMX, particleMY, particleMZ);
        world.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, particleMX, particleMY, particleMZ);
    }

    @Override
    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if (entity.level().isClientSide() && ConfigClient.MISSILE_ENGINE_SMOKE)
        {
            Pos position = new Pos(entity);
            // The distance of the smoke relative
            // to the missile.
            double distance = -1.2f;
            // The delta Y of the smoke.
            double y = Math.sin(Math.toRadians(entity.getXRot())) * distance;
            // The horizontal distance of the
            // smoke.
            double dH = Math.cos(Math.toRadians(entity.getXRot())) * distance;
            // The delta X and Z.
            double x = Math.sin(Math.toRadians(entity.getYRot())) * dH;
            double z = Math.cos(Math.toRadians(entity.getYRot())) * dH;
            position = position.add(x, y, z);

            final Vec3 motion = entity.getDeltaMovement();
            for (int i = 0; i < 10; i++)
            {
                spawnAirParticle(entity.level(),
                    position.x(), position.y(), position.z(),
                    -motion.x * 0.5, -motion.y * 0.5, -motion.z * 0.5,
                    flightLogic.engineSmokeRed(entity), flightLogic.engineSmokeGreen(entity), flightLogic.engineSmokeBlue(entity),
                    (int) Math.max(1d, 6d * (1 / (1 + entity.getY() / 100))), 100);
                position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
            }
        }
    }

    @Override
    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if (ConfigClient.MISSILE_LAUNCH_SMOKE && entity.level() instanceof ClientLevel world)
        {
            final RandomSource random = world.random;

            double posX = entity.getX();
            double posY = entity.getY() - 1.2; //TODO get missile height from type
            double posZ = entity.getZ();

            //Spawn smoke TODO add config for smoke amount
            for (int smokeCount = 0; smokeCount < 10; smokeCount++)
            {
                //Randomize flight direction down in a cone
                final double velX = (random.nextFloat() - random.nextFloat()) * 0.3;
                final double velY = 1 - (random.nextFloat() * 0.5);
                final double velZ = (random.nextFloat() - random.nextFloat()) * 0.3;

                //spawn smoke
                final ParticleLauncherSmoke particleAirParticleICBM = new ParticleLauncherSmoke(world,
                    posX, posY, posZ,
                    velX, -velY, velZ,
                    1 + 2 * random.nextFloat()
                );
                particleAirParticleICBM.setColor(1, 1, 1, true);
                particleAirParticleICBM.setAge(180);
                Minecraft.getInstance().particleEngine.add(particleAirParticleICBM);
            }
        }
    }
}
// PORTED
