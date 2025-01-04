package icbm.classic.content.entity;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityShrapnel extends EntityProjectile<EntityShrapnel> {
    public EntityShrapnel(EntityType<EntityShrapnel> entityTypeIn, World world) {
        super(entityTypeIn, world);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return ConfigBlast.shrapnel.damage; //TODO scale with velocity
    }
}
