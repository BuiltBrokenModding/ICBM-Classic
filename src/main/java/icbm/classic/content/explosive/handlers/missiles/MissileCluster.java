package icbm.classic.content.explosive.handlers.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.EntityMissile.MissileType;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** @author Calclavia */
public class MissileCluster extends Missile
{
    public static final int MAX_CLUSTER = 12;
    protected double spread = 20;

    public MissileCluster(String name, EnumTier tier)
    {
        super(name, tier);
        this.hasBlock = false;
        //this.missileModelPath = "missiles/tier2/missile_head_cluster.obj";
    }

    @Override
    public void update(EntityMissile missileCluster)
    {
        final int missileIndex = missileCluster.missileCount;
        if (missileCluster.motionY < -0.5) //TODO why use motion as the trigger?
        {
            if (missileCluster.missileCount < MAX_CLUSTER)
            {
                if (!missileCluster.world.isRemote)
                {
                    //Create missile
                    EntityMissile missile = new EntityMissile(missileCluster.world);

                    //Set position
                    Pos position = new Pos((IPos3D) missileCluster).add(getSpreadForMissile(missileIndex));
                    missile.setPosition(position.x(), position.y(), position.z());

                    //Set data
                    missile.launcherPos = missileCluster.launcherPos;
                    missile.explosiveID = Explosives.CONDENSED;
                    missile.missileType = MissileType.MISSILE;
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

                        //Set
                        missile.launch(pos);
                    }

                    //Spawn
                    missileCluster.world.spawnEntity(missile);
                }

                //Setup for next missile
                missileCluster.protectionTime = 20;
                missileCluster.missileCount++;
            }
            else
            {
                missileCluster.setDead();
            }
        }
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
            x = Math.cos(theta) * 5;
            z = Math.sin(theta) * 5;
        }
        return new Pos(x, y, z);
    }

    protected double getAngleForMissile(int index)
    {
        return (index / (double) MAX_CLUSTER) * Math.PI * 2;
    }


    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastTNT(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6).setDestroyItems().explode();
    }

    @Override
    public boolean isCruise()
    {
        return false;
    }
}
