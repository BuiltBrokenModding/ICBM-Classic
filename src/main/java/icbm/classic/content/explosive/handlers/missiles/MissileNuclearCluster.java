package icbm.classic.content.explosive.handlers.missiles;

import icbm.classic.config.ConfigMissile;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastNuclear;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MissileNuclearCluster extends MissileCluster
{
    public MissileNuclearCluster()
    {
        super("nuclearCluster", EnumTier.THREE);
        this.hasBlock = false;
    }

    @Override
    protected int getMissileSpawnCount()
    {
        return ConfigMissile.NUCLEAR_CLUSTER_SIZE;
    }

    @Override
    protected EntityMissile createMissile(EntityMissile missileCluster, int index)
    {
        EntityMissile missile = new EntityMissile(missileCluster.world);
        missile.explosiveID = Explosives.NUCLEAR;
        return missile;
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
