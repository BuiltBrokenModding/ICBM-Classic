package icbm.classic.client;

import com.builtbroken.jlib.data.vector.IPos3D;

import icbm.classic.CommonProxy;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.client.fx.ParticleAirICBM;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.content.entity.missile.MissileFlightType;
import icbm.classic.content.entity.missile.logic.BallisticFlightLogic;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
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
    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir)
    {
        if (entity.world.isRemote)
        {
            if (flightLogic instanceof BallisticFlightLogic)
            {
                if (entity.motionY > -1)
                {
                    if (entity.world.isRemote && entity.motionY > -1)
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

                        if (((BallisticFlightLogic) flightLogic).getPreLaunchSmokeTimer() > 0 && ticksInAir <= BallisticFlightLogic.MAX_PRE_LAUNCH_SMOKE_TICKS) // pre-launch phase
                        {
                            Pos launcherSmokePosition = position.sub(0, 2, 0);
                            if (((BallisticFlightLogic)flightLogic).launcherHasAirBelow)
                            {
                                Pos velocity = new Pos(0, -1, 0).addRandom(entity.world.rand, 0.5);
                                for (int i = 0; i < 10; i++)
                                {
                                    // smoke below the launcher
                                    spawnAirParticle(entity.world,
                                            launcherSmokePosition.x(), launcherSmokePosition.y(), launcherSmokePosition.z(),
                                            velocity.x(), velocity.y(), velocity.z(),
                                            1, 1, 1,
                                            8, 180);

                                    launcherSmokePosition = launcherSmokePosition
                                            .multiply(1 - 0.025 * Math.random(),
                                                    1 - 0.025 * Math.random(),
                                                    1 - 0.025 * Math.random());
                                }
                            }

                            for (int i = 0; i < 5; i++)
                            {
                                Pos velocity = new Pos(0, 0.25, 0).addRandom(entity.world.rand, 0.125);
                                // smoke below the launcher
                                spawnAirParticle(entity.world,
                                        position.x(), position.y(), position.z(),
                                        velocity.x(), velocity.y(), velocity.z(),
                                        1, 1, 1,
                                        5,40);
                            }
                        }
                        else
                        {
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
                            1, 1, 1,
                            (int) Math.max(1d, 6d * (1 / (1 + entity.posY / 100))), 100);
                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                }
            }
        }
    }
}
