package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityMissile.MissileType;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastTNT;
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
    public void update(EntityMissile missileObj)
    {
        if (missileObj.motionY < -0.5)
        {
            if (missileObj.missileCount < MAX_CLUSTER)
            {
                if (!missileObj.world.isRemote)
                {
                    Pos position = new Pos((IPos3D)missileObj);
                    EntityMissile missile = new EntityMissile(missileObj.world);
                    missile.setPosition(position.x(), position.y(), position.z()); //TODO spread to avoid collision
                    missile.launcherPos = position;
                    missile.explosiveID = Explosives.CONDENSED;

                    double radius = spread;
                    double theta = 0;
                    double x = 0;
                    double y = 0;
                    double z = 0;
                    //TODO make spread equal to a 30 degree angle from center point

                    if (missileObj.missileCount > 0)
                    {
                        theta = (missileObj.missileCount / 12.0) * Math.PI * 2;

                        x = radius * Math.cos(theta);
                        missile.posX += Math.cos(theta) * 5;

                        z = radius * Math.sin(theta);
                        missile.posZ += Math.sin(theta) * 5;
                    }

                    missile.missileType = MissileType.CruiseMissile;
                    missile.protectionTime = 20 + missileObj.targetHeight - 1;

                    missile.launch(missileObj.targetVector.add(new Pos(x, y, z)));
                    missileObj.world.spawnEntity(missile);
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
