package icbm.classic.client;

import com.builtbroken.jlib.data.vector.IPos3D;

import icbm.classic.CommonProxy;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.client.fx.ParticleAirICBM;
import icbm.classic.client.fx.ParticleLauncherSmoke;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.client.render.entity.layer.LayerChickenHelmet;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.mods.ModInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();
        final Render render = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(EntityChicken.class);
        if(render instanceof RenderChicken) {
            ((RenderChicken) render).addLayer(new LayerChickenHelmet((RenderChicken) render));
        }
    }

    @Override
    public void spawnSmoke(World world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world != null)
        {
            ParticleSmokeICBM particleSmokeICBM = new ParticleSmokeICBM(world, position, v, v1, v2, scale);
            particleSmokeICBM.setColor(red, green, blue, true);
            particleSmokeICBM.setAge(ticksToLive);
            Minecraft.getMinecraft().effectRenderer.addEffect(particleSmokeICBM);
        }
    }

    @Override
    public void spawnAirParticle(World world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world != null)
        {
            ParticleAirICBM particleAirParticleICBM = new ParticleAirICBM(world, x, y, z, v, v1, v2, scale);
            particleAirParticleICBM.setColor(red, green, blue, true);
            particleAirParticleICBM.setAge(ticksToLive);
            Minecraft.getMinecraft().effectRenderer.addEffect(particleAirParticleICBM);
        }
    }

    @Override
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

        world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
            (particleX + sourceX) / 2.0D,
            (particleY + sourceY) / 2.0D,
            (particleZ + sourceZ) / 2.0D,
            particleMX, particleMY, particleMZ);
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particleX, particleY, particleZ, particleMX, particleMY, particleMZ);
    }

    @Override
    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if (entity.world.isRemote)
        {
            if (flightLogic instanceof BallisticFlightLogic)
            {
                if (entity.motionY > -1)
                {
                    Pos position = new Pos((IPos3D) entity);
                    // The distance of the smoke relative
                    // to the missile.
                    double distance = -1.2f;
                    // The delta Y of the smoke.
                    double y = Math.sin(Math.toRadians(entity.rotationPitch)) * distance;
                    // The horizontal distance of the
                    // smoke.
                    double dH = Math.cos(Math.toRadians(entity.rotationPitch)) * distance;
                    // The delta X and Z.
                    double x = Math.sin(Math.toRadians(entity.rotationYaw)) * dH;
                    double z = Math.cos(Math.toRadians(entity.rotationYaw)) * dH;
                    position = position.add(x, y, z);

                    ((BallisticFlightLogic)flightLogic).getLastSmokePos().add(position);

                    Pos lastPos = null;
                    if (((BallisticFlightLogic)flightLogic).getLastSmokePos().size() > 5)
                    {
                        lastPos = ((BallisticFlightLogic)flightLogic).getLastSmokePos().get(0);
                        ((BallisticFlightLogic)flightLogic).getLastSmokePos().remove(0);
                    }

                    spawnAirParticle(entity.world,
                        position.x(), position.y(), position.z(),
                        -entity.motionX * 0.75, -entity.motionY * 0.75, -entity.motionZ * 0.75,
                        1, 0.75f, 0,
                        5, 10);

                    if (ticksInAir > 5 && lastPos != null)
                    {
                        for (int i = 0; i < 10; i++)
                        {
                            spawnAirParticle(entity.world,
                                lastPos.x(), lastPos.y(), lastPos.z(),
                                -entity.motionX * 0.5, -entity.motionY * 0.5, -entity.motionZ * 0.5,
                                1, 1, 1,
                                (int) Math.max(1d, 6d * (1 / (1 + entity.posY / 100))), 100);
                            position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                        }
                    }
                }
            }
            else
            {
                Pos position = new Pos((IPos3D) entity);
                // The distance of the smoke relative
                // to the missile.
                double distance = -1.2f;
                // The delta Y of the smoke.
                double y = Math.sin(Math.toRadians(entity.rotationPitch)) * distance;
                // The horizontal distance of the
                // smoke.
                double dH = Math.cos(Math.toRadians(entity.rotationPitch)) * distance;
                // The delta X and Z.
                double x = Math.sin(Math.toRadians(entity.rotationYaw)) * dH;
                double z = Math.cos(Math.toRadians(entity.rotationYaw)) * dH;
                position = position.add(x, y, z);

                for (int i = 0; i < 10; i++)
                {
                    spawnAirParticle(entity.world,
                            position.x(), position.y(), position.z(),
                            -entity.motionX * 0.5, -entity.motionY * 0.5, -entity.motionZ * 0.5,
                        flightLogic.engineSmokeRed(entity), flightLogic.engineSmokeGreen(entity), flightLogic.engineSmokeBlue(entity),
                            (int) Math.max(1d, 6d * (1 / (1 + entity.posY / 100))), 100);
                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                }
            }
        }
    }

    @Override
    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        final World world = entity.world;
        final Random random = world.rand;

        double posX = entity.posX;
        double posY = entity.posY - 1.2; //TODO get missile height from type
        double posZ = entity.posZ;

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
            Minecraft.getMinecraft().effectRenderer.addEffect(particleAirParticleICBM);
        }
    }
}
