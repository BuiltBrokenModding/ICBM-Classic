package icbm.classic.prefab.entity;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.saving.NbtSaveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Base entity class to be shared by most entities
 * Created by Robin on 1/24/2015.
 */
public abstract class EntityICBM extends Entity
{
    /** Does the entity have HP to take damage. */
    protected boolean hasHealth = false;

    private static final DataParameter<Float> HEALTH = EntityDataManager.<Float>createKey(EntityICBM.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> MAX_HEALTH = EntityDataManager.<Float>createKey(EntityICBM.class, DataSerializers.FLOAT);

    public EntityICBM(EntityType<?> entityTypeIn, World world)
    {
        super(entityTypeIn, world);
    }

    @Override
    protected void registerData()
    {
        this.dataManager.register(HEALTH, 1f);
        this.dataManager.register(MAX_HEALTH, 1f);
    }

    public float getHealth()
    {
        return this.dataManager.get(HEALTH);
    }

    public <T> T setHealth(float health)
    {
        this.dataManager.set(HEALTH, MathHelper.clamp(health, 0.0F, this.getMaxHealth()));
        return (T) this;
    }

    public float getMaxHealth()
    {
        return this.dataManager.get(MAX_HEALTH);
    }

    public <T> T  setMaxHealth(float hp) {
        this.dataManager.set(MAX_HEALTH, hp);
        return (T) this;
    }

    public <T> T  initHealth(float hp) {
        this.setMaxHealth(hp);
        this.setHealth(hp);
        return (T) this;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
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
                if (entity instanceof PlayerEntity)
                {
                    if (((PlayerEntity) entity).abilities.isCreativeMode)
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
    protected void destroy() {
        this.remove();
    }

    /**
     * Gets the predicted position
     *
     * @param t - number of ticks to predicted
     * @return predicted position of the project
     */
    public Vec3d getPredictedPosition(int t)
    {
        return new Vec3d(posX + getMotion().x * t, posY + getMotion().y * t, posZ + getMotion().z * t);
    }

    @Override
    protected void readAdditional(CompoundNBT nbt)
    {
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt)
    {
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityICBM> SAVE_LOGIC = new NbtSaveHandler<EntityICBM>()
        .mainRoot()
        /* */.nodeFloat("health", EntityICBM::getHealth, EntityICBM::setHealth)
        /* */.nodeFloat("health_max", EntityICBM::getMaxHealth, EntityICBM::setMaxHealth)
        .base();
}
