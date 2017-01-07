package icbm.classic.content.explosive.ex.missiles;

import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityMissile.MissileType;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.blast.BlastNuclear;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import static java.lang.Math.random;

public class MissileNuclearCluster extends MissileCluster
{
    public MissileNuclearCluster()
    {
        super("nuclearCluster", 3);
        this.hasBlock = false;
    }

    public static final int MAX_CLUSTER = 4;

    @Override
    public void update(EntityMissile missileObj)
    {
        if (missileObj.motionY < -0.5)
        {
            if (missileObj.missileCount < MAX_CLUSTER)
            {
                if (!missileObj.worldObj.isRemote)
                {
                    Pos position = new Pos(missileObj);
                    EntityMissile clusterMissile = new EntityMissile(missileObj.worldObj, position, new Pos(missileObj), Explosive.nuclear.getID());
                    missileObj.worldObj.spawnEntityInWorld(clusterMissile);
                    clusterMissile.missileType = MissileType.CruiseMissile;
                    clusterMissile.protectionTime = 20;
                    clusterMissile.launch(Pos.translate(missileObj.targetVector, new Pos((missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30)));
                }

                missileObj.protectionTime = 20;
                missileObj.missileCount++;
            }
            else
            {
                missileObj.setDead();
            }
        }
    }

    @Override
    public void createExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastNuclear(world, entity, x, y, z, 30, 50).setNuclear().explode();
    }

    @Override
    public boolean isCruise()
    {
        return false;
    }
}
