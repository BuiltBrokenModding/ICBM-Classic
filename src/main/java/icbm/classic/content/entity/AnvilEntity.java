package icbm.classic.content.entity;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class AnvilEntity extends EntityProjectile<AnvilEntity> {
    public AnvilEntity(EntityType<AnvilEntity> entityTypeIn, World world) {
        super(entityTypeIn, world);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return ConfigBlast.anvil.damage; //TODO scale with velocity
    }
}
