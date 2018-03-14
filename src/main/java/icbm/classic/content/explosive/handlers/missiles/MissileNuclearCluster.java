package icbm.classic.content.explosive.handlers.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.EntityMissile.MissileType;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastNuclear;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static java.lang.Math.random;

public class MissileNuclearCluster extends MissileCluster
{
    public MissileNuclearCluster()
    {
        super("nuclearCluster", EnumTier.THREE);
        this.hasBlock = false;
    }

    public static final int MAX_CLUSTER = 4;

    @Override
    public void update(EntityMissile missileCluster)
    {
        if (missileCluster.motionY < -0.5)
        {
            if (missileCluster.missileCount < MAX_CLUSTER)
            {
                if (!missileCluster.world.isRemote)
                {
                    Pos position = new Pos((IPos3D) missileCluster);

                    EntityMissile clusterMissile = new EntityMissile(missileCluster.world);
                    clusterMissile.setPosition(position.x(), position.y(), position.z()); //TODO randomize spread to prevent collisions
                    clusterMissile.explosiveID = Explosives.NUCLEAR;

                    missileCluster.world.spawnEntity(clusterMissile);
                    clusterMissile.missileType = MissileType.CruiseMissile;
                    clusterMissile.protectionTime = 20;
                    clusterMissile.launch(missileCluster.targetPos.add(new Pos((missileCluster.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileCluster.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileCluster.missileCount - MAX_CLUSTER / 2) * random() * 30)));
                }

                missileCluster.protectionTime = 20;
                missileCluster.missileCount++;
            }
            else
            {
                missileCluster.setDead();
            }
        }
    }

    @Override
    public void createExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastNuclear(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 50).setNuclear().explode();
    }

    @Override
    public boolean isCruise()
    {
        return false;
    }
}
