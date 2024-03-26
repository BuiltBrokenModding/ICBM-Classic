package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.client.particle.IcbmSmokeParticle;
import icbm.classic.client.particle.LauncherSmokeParticle;
import icbm.classic.client.particle.StaleSmokeParticle;
import icbm.classic.client.render.entity.layer.LayerChickenHelmet;
import icbm.classic.config.ConfigClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
        EntityRenderer<?> render = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(EntityType.CHICKEN);
        if (render instanceof ChickenRenderer chicken) {
            chicken.addLayer(new LayerChickenHelmet(chicken));
        }
    }

    @Override
    public void spawnSmoke(Level level, Vec3 position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive) {
        if (level != null) {
            IcbmSmokeParticle icbmSmokeParticle = new IcbmSmokeParticle(level, position, v, v1, v2, scale);
            icbmSmokeParticle.setColor(red, green, blue, true);
            icbmSmokeParticle.setLifetime(ticksToLive);
            Minecraft.getInstance().levelRenderer.addParticle(icbmSmokeParticle);
        }
    }

    @Override
    public void spawnAirParticle(ClientLevel level, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive) {
        if (level != null) {
            StaleSmokeParticle particleAirParticleICBM = new StaleSmokeParticle(level, x, y, z, v, v1, v2, scale);
            particleAirParticleICBM.setColor(red, green, blue, true);
            particleAirParticleICBM.setLifetime(ticksToLive);
            Minecraft.getInstance().levelRenderer.addParticle(particleAirParticleICBM);
        }
    }

    @Override
    public void spawnExplosionParticles(Level level, double sourceX, double sourceY, double sourceZ, double blastScale,
                                        BlockPos blockPos) {
        //Random position near destroyed block
        final double particleX = (blockPos.getX() + level.getRandom().nextFloat());
        final double particleY = (blockPos.getY() + level.getRandom().nextFloat());
        final double particleZ = (blockPos.getZ() + level.getRandom().nextFloat());

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
        multiplier *= (level.getRandom().nextFloat() * level.getRandom().nextFloat() + 0.3F);
        particleMX *= multiplier;
        particleMY *= multiplier;
        particleMZ *= multiplier;

        level.addParticle(EnumParticleTypes.EXPLOSION_NORMAL,
            (particleX + sourceX) / 2.0D,
            (particleY + sourceY) / 2.0D,
            (particleZ + sourceZ) / 2.0D,
            particleMX, particleMY, particleMZ);
        level.addParticle(EnumParticleTypes.SMOKE_NORMAL, particleX, particleY, particleZ, particleMX, particleMY, particleMZ);
    }

    @Override
    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {
        if (!entity.level() ().isClientSide() && ConfigClient.MISSILE_ENGINE_SMOKE)
        {
            Vec3 position = entity.position();
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

            for (int i = 0; i < 10; i++) {
                spawnAirParticle(entity.level() (),
                    position.x(), position.y(), position.z(),
                    -entity.motionX * 0.5, -entity.motionY * 0.5, -entity.motionZ * 0.5,
                    flightLogic.engineSmokeRed(entity), flightLogic.engineSmokeGreen(entity), flightLogic.engineSmokeBlue(entity),
                    (int) Math.max(1d, 6d * (1 / (1 + entity.getY() / 100))), 100);
                position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
            }
        }
    }

    @Override
    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {
        if (ConfigClient.MISSILE_LAUNCH_SMOKE) {
            Level level = entity.level();
            RandomSource random = level.getRandom();

            double posX = entity.getX();
            double posY = entity.getY() - 1.2; //TODO get missile height from type
            double posZ = entity.getZ();

            //Spawn smoke TODO add config for smoke amount
            for (int smokeCount = 0; smokeCount < 10; smokeCount++) {
                //Randomize flight direction down in a cone
                final double velX = (random.nextFloat() - random.nextFloat()) * 0.3;
                final double velY = 1 - (random.nextFloat() * 0.5);
                final double velZ = (random.nextFloat() - random.nextFloat()) * 0.3;

                //spawn smoke
                final LauncherSmokeParticle particleAirParticleICBM = new LauncherSmokeParticle(level,
                    posX, posY, posZ,
                    velX, -velY, velZ,
                    1 + 2 * random.nextFloat()
                );
                particleAirParticleICBM.setColor(1, 1, 1, true);
                particleAirParticleICBM.setLifetime(180);
                Minecraft.getInstance().levelRenderer.addParticle(particleAirParticleICBM);
            }
        }
    }
}
