package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityMissile.MissileType;
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
    public void update(EntityMissile missileObj)
    {
        if (missileObj.motionY < -0.5)
        {
            if (missileObj.missileCount < MAX_CLUSTER)
            {
                if (!missileObj.world.isRemote)
                {
                    Pos position = new Pos((IPos3D)missileObj);

                    EntityMissile clusterMissile = new EntityMissile(missileObj.world);
                    clusterMissile.setPosition(position.x(), position.y(), position.z()); //TODO randomize spread to prevent collisions
                    clusterMissile.explosiveID = Explosives.NUCLEAR;

                    missileObj.world.spawnEntity(clusterMissile);
                    clusterMissile.missileType = MissileType.CruiseMissile;
                    clusterMissile.protectionTime = 20;
                    clusterMissile.launch(missileObj.targetVector.add(new Pos((missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30, (missileObj.missileCount - MAX_CLUSTER / 2) * random() * 30)));
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
