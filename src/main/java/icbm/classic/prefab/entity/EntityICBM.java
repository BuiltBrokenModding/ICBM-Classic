package icbm.classic.prefab.entity;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Base entity class to be shared by most entities
 * Created by Robin on 1/24/2015.
 */
public abstract class EntityICBM extends Entity implements IWorldPosition
{
    /** Does the entity have HP to take damage. */
    protected boolean hasHealth = false;

    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(EntityICBM.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_HEALTH = SynchedEntityData.defineId(EntityICBM.class, EntityDataSerializers.FLOAT);

    public EntityICBM(EntityType<? extends EntityICBM> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(HEALTH, 1f);
        builder.define(MAX_HEALTH, 1f);
    }

    public float getHealth()
    {
        return this.entityData.get(HEALTH);
    }

    public <T> T setHealth(float health)
    {
        this.entityData.set(HEALTH, Mth.clamp(health, 0.0F, this.getMaxHealth()));
        return (T) this;
    }

    public float getMaxHealth()
    {
        return this.entityData.get(MAX_HEALTH);
    }

    public <T> T setMaxHealth(float hp)
    {
        this.entityData.set(MAX_HEALTH, hp);
        return (T) this;
    }

    public <T> T initHealth(float hp)
    {
        this.setMaxHealth(hp);
        this.setHealth(hp);
        return (T) this;
    }

    @Override
    public boolean hurt(DamageSource source, float damage)
    {
        // PORT: health and destruction are server authoritative, client calls (Player#attack) would desync the entity
        if (this.level().isClientSide())
        {
            return false;
        }
        if (hasHealth && damage > 0 && canDamage(this, source))
        {
            this.setHealth(Math.max(getHealth() - damage, 0));
            if (getHealth() <= 0)
            {
                onDestroyedBy(source, damage);
            }
            return true;
        }
        return false;
    }

    /**
     * Can damage be applied at all to the entity
     *
     * @param entity - entity being attacked
     * @return true if the entity can be damaged
     */
    public boolean canDamage(Entity entity, DamageSource source)
    {
        if (!entity.isInvulnerableTo(source) && entity.isAlive())
        {
            if (entity instanceof LivingEntity)
            {
                if (entity instanceof Player)
                {
                    if (((Player) entity).isCreative())
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Called when the entity is killed
     */
    protected void onDestroyedBy(DamageSource source, float damage)
    {
        this.destroy();
    }

    /**
     * Called when the entity expires or
     * is destroyed in some way.
     */
    protected void destroy()
    {
        this.discard();
    }

    /**
     * Sets the position based on the bounding box
     */
    protected void alignToBounds()
    {
        final AABB box = this.getBoundingBox();
        // PORT: 1.12 getYOffset() was always 0 for these entities
        this.setPosRaw((box.minX + box.maxX) / 2.0D, box.minY - (double) this.getBbHeight(), (box.minZ + box.maxZ) / 2.0D);
    }

    /**
     * Gets the predicted position
     *
     * @param t - number of ticks to predicted
     * @return predicted position of the project
     */
    public Vec3 getPredictedPosition(int t)
    {
        final Vec3 motion = getDeltaMovement();
        return new Vec3(getX() + motion.x * t, getY() + motion.y * t, getZ() + motion.z * t);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityICBM> SAVE_LOGIC = new NbtSaveHandler<EntityICBM>()
        .mainRoot()
        // PORT: max loads first and health is restored without the clamp, setHealth clamps to getMaxHealth which
        // subclasses may derive from data that loads later (explosive tier), cutting health on every reload
        /* */.nodeFloat("health_max", EntityICBM::getMaxHealth, EntityICBM::setMaxHealth)
        /* */.nodeFloat("health", EntityICBM::getHealth, (e, h) -> e.entityData.set(HEALTH, h))
        .base();

    @Override
    public Level world()
    {
        return level();
    }

    @Override
    public double x()
    {
        return getX();
    }

    @Override
    public double y()
    {
        return getY();
    }

    @Override
    public double z()
    {
        return getZ();
    }

    public Pos getVelocity()
    {
        final Vec3 motion = getDeltaMovement();
        return new Pos(motion.x, motion.y, motion.z); //TODO make wrapper object
    }

    /** PORT: 1.12 client velocity packets called Entity#setVelocity, 1.21 calls {@link #lerpMotion} */
    @Override
    public void lerpMotion(double xx, double yy, double zz)
    {
        setVelocity(xx, yy, zz);
    }

    public void setVelocity(double xx, double yy, double zz)
    {
        // Client side only gets 5 decimal places due to packet storing as int then converting back to double using divide by 8000... effectively a float
        setMotionVector(xx, yy, zz);
    }

    public void setMotionVector(double xx, double yy, double zz)
    {
        this.setDeltaMovement(xx, yy, zz);
    }
}
// PORTED
