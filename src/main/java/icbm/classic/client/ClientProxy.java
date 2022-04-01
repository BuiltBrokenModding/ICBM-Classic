package icbm.classic.client;

import com.builtbroken.jlib.data.vector.IPos3D;

import icbm.classic.CommonProxy;
import icbm.classic.client.fx.ParticleAirICBM;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.MissileFlightType;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
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
    public void spawnMissileSmoke(EntityMissile missile)
    {
        if (missile.world.isRemote)
        {
            if (missile.missileType == MissileFlightType.PAD_LAUNCHER)
            {
                if (missile.motionY > -1)
                {
                    if (missile.world.isRemote && missile.motionY > -1)
                    {
                        if (missile.launcherHasAirBelow == -1)
                        {
                            BlockPos bp = new BlockPos(Math.signum(missile.posX) * Math.floor(Math.abs(missile.posX)), missile.posY - 2, Math.signum(missile.posZ) * Math.floor(Math.abs(missile.posZ)));
                            missile.launcherHasAirBelow = missile.world.isAirBlock(bp) ? 1 : 0;
                        }
                        Pos position = new Pos((IPos3D) missile);
                        // The distance of the smoke relative
                        // to the missile.
                        double distance = -1.2f;
                        // The delta Y of the smoke.
                        double y = Math.sin(Math.toRadians(missile.rotationPitch)) * distance;
                        // The horizontal distance of the
                        // smoke.
                        double dH = Math.cos(Math.toRadians(missile.rotationPitch)) * distance;
                        // The delta X and Z.
                        double x = Math.sin(Math.toRadians(missile.rotationYaw)) * dH;
                        double z = Math.cos(Math.toRadians(missile.rotationYaw)) * dH;
                        position = position.add(x, y, z);

                        if (missile.preLaunchSmokeTimer > 0 && missile.ticksInAir <= missile.getMaxPreLaunchSmokeTimer()) // pre-launch phase
                        {
                            Pos launcherSmokePosition = position.sub(0, 2, 0);
                            if (missile.launcherHasAirBelow == 1)
                            {
                                Pos velocity = new Pos(0, -1, 0).addRandom(missile.world.rand, 0.5);
                                for (int i = 0; i < 10; i++)
                                {
                                    // smoke below the launcher
                                    spawnAirParticle(missile.world,
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
                                Pos velocity = new Pos(0, 0.25, 0).addRandom(missile.world.rand, 0.125);
                                // smoke below the launcher
                                spawnAirParticle(missile.world,
                                        position.x(), position.y(), position.z(),
                                        velocity.x(), velocity.y(), velocity.z(),
                                        1, 1, 1,
                                        5,40);
                            }
                        }
                        else
                        {
                            missile.getLastSmokePos().add(position);
                            Pos lastPos = null;
                            if (missile.getLastSmokePos().size() > 5)
                            {
                                lastPos = missile.getLastSmokePos().get(0);
                                missile.getLastSmokePos().remove(0);
                            }
                            spawnAirParticle(missile.world,
                                    position.x(), position.y(), position.z(),
                                    -missile.motionX * 0.75, -missile.motionY * 0.75, -missile.motionZ * 0.75,
                                    1, 0.75f, 0,
                                    5, 10);
                            if (missile.ticksInAir > 5 && lastPos != null)
                            {
                                for (int i = 0; i < 10; i++)
                                {
                                    spawnAirParticle(missile.world,
                                            lastPos.x(), lastPos.y(), lastPos.z(),
                                            -missile.motionX * 0.5, -missile.motionY * 0.5, -missile.motionZ * 0.5,
                                            1, 1, 1,
                                            (int) Math.max(1d, 6d * (1 / (1 + missile.posY / 100))), 100);
                                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                Pos position = new Pos((IPos3D) missile);
                // The distance of the smoke relative
                // to the missile.
                double distance = -1.2f;
                // The delta Y of the smoke.
                double y = Math.sin(Math.toRadians(missile.rotationPitch)) * distance;
                // The horizontal distance of the
                // smoke.
                double dH = Math.cos(Math.toRadians(missile.rotationPitch)) * distance;
                // The delta X and Z.
                double x = Math.sin(Math.toRadians(missile.rotationYaw)) * dH;
                double z = Math.cos(Math.toRadians(missile.rotationYaw)) * dH;
                position = position.add(x, y, z);

                for (int i = 0; i < 10; i++)
                {
                    spawnAirParticle(missile.world,
                            position.x(), position.y(), position.z(),
                            -missile.motionX * 0.5, -missile.motionY * 0.5, -missile.motionZ * 0.5,
                            1, 1, 1,
                            (int) Math.max(1d, 6d * (1 / (1 + missile.posY / 100))), 100);
                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                }
            }
        }
    }
}
