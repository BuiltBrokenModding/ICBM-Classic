package icbm.classic.content.explosive.handlers.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.missile.MissileFlightType;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.api.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** @author Calclavia */
public class MissileCluster //extends Missile
{
    protected double spread = 30;

    public MissileCluster(String name, EnumTier tier)
    {
        //super(name, tier);
        //this.hasBlock = false;
        //this.missileModelPath = "missiles/tier2/missile_head_cluster.obj";
    }

    protected boolean shouldTrigger(EntityMissile missileCluster)
    {
        return missileCluster.motionY < -0.5;  //TODO why use motion as the trigger?
    }

    //@Override
    public void update(EntityMissile missileCluster)
    {
        //Check if we can trigger
        if (!missileCluster.isExploding() && shouldTrigger(missileCluster))
        {
            missileCluster.isExploding = true;
            missileCluster.setDead();

            for (int missileIndex = 0; missileIndex <= getMissileSpawnCount(); missileIndex++)
            {
                if (!missileCluster.world.isRemote)
                {
                    //Create missile
                    EntityMissile missile = createMissile(missileCluster, missileIndex);

                    //Set position
                    Pos position = new Pos((IPos3D) missileCluster).add(getSpreadForMissile(missileIndex));
                    missile.setPosition(position.x(), position.y(), position.z());

                    //Set data
                    missile.launcherPos = missileCluster.launcherPos;
                    missile.missileType = MissileFlightType.DEAD_AIM;
                    missile.protectionTime = 20 + missileCluster.targetHeight - 1;
                    missile.ticksInAir = missileCluster.ticksInAir;

                    //Set target
                    if (missileCluster.targetPos != null)
                    {
                        //Use existing target to offset
                        missile.launch(missileCluster.targetPos.add(getTargetDeltaForMissile(missileIndex)));
                    }
                    else
                    {
                        //Calculate target as the vector
                        Pos pos = new Pos(missileCluster.motionX, missileCluster.motionY, missileCluster.motionZ).normalize();
                        pos = pos.multiply(200); //In theory this would be strait down
                        pos = pos.add(missileCluster); //Add position
                        pos = pos.add(getTargetDeltaForMissile(missileIndex)); //offset
                        pos = pos.addRandom(missileCluster.world.rand, 4);

                        //Set
                        missile.launch(pos);
                    }

                    //Spawn
                    missileCluster.world.spawnEntity(missile);
                }
            }
        }
    }

    protected int getMissileSpawnCount()
    {
        return ConfigMissile.CLUSTER_SIZE;
    }

    protected EntityMissile createMissile(EntityMissile missileCluster, int index)
    {
        EntityMissile missile = new EntityMissile(missileCluster.world);
        missile.explosiveID = Explosives.CONDENSED;
        return missile;
    }


    /**
     * Calculates offset for the missile target
     *
     * @param index - spawn index of the missile, used for angle
     * @return offset for target
     */
    protected Pos getTargetDeltaForMissile(int index)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        if (index > 0)
        {
            double theta = getAngleForMissile(index);
            x = spread * Math.cos(theta);
            z = spread * Math.sin(theta);
        }
        return new Pos(x, y, z);
    }

    /**
     * Calculates distance xyz to spawn the missile from the host
     *
     * @param index - spawn index of the missile, used for angle
     * @return position to offset from host
     */
    protected Pos getSpreadForMissile(int index)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        if (index > 0)
        {
            double theta = getAngleForMissile(index);
            x = Math.cos(theta) * getSpreadDistanceForMissile(index);
            z = Math.sin(theta) * getSpreadDistanceForMissile(index);
        }
        return new Pos(x, y, z);
    }

    protected double getSpreadDistanceForMissile(int index)
    {
        return index % 2 == 0 ? 4 : 7;
    }


    protected double getAngleForMissile(int index)
    {
        return (index / (double) getMissileSpawnCount()) * Math.PI * 2;
    }


    //@Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastTNT(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6 * scale).setDestroyItems().runBlast();
    }

    //@Override
    public boolean isCruise()
    {
        return false;
    }
}
