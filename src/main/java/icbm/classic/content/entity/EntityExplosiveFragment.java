package icbm.classic.content.entity;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

/**
 * Projectile that explodes into fragments on impact used with Fragmentation Explosive
 */
public class EntityExplosiveFragment extends EntityProjectile<EntityExplosiveFragment> {
    public EntityExplosiveFragment(EntityType<EntityExplosiveFragment> entityTypeIn, World world) {
        super(entityTypeIn, world);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return ConfigBlast.fragmentation.damage; //TODO scale with velocity
    }

    @Override
    protected void onImpact(RayTraceResult hit)
    {
        if (!this.isAlive() && !this.world.isRemote)
        {
            //TODO add config toggle to damage blocks or just do entity damage
            this.world.createExplosion(this, posX, posY, posZ, ConfigBlast.fragmentation.explosionSize, Explosion.Mode.DESTROY);
        }
        this.remove();
    }
}
