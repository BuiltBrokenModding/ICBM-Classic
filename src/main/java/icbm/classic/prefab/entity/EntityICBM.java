package icbm.classic.prefab.entity;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Base entity class to be shared by most entities
 * Created by robert on 1/24/2015.
 */
public abstract class EntityICBM extends Entity implements IWorldPosition
{
    /** Does the entity have HP to take damage. */
    protected boolean hasHealth = false;

    private static final DataParameter<Float> HEALTH = EntityDataManager.<Float>createKey(EntityICBM.class, DataSerializers.FLOAT);

    public EntityICBM(World world)
    {
        super(world);
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(HEALTH, Float.valueOf(0));
    }

    public float getHealth()
    {
        return ((Float)this.dataManager.get(HEALTH)).floatValue();
    }

    public void setHealth(float health)
    {
        this.dataManager.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0F, this.getMaxHealth())));
    }

    public float getMaxHealth()
    {
        return 5;
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
        if (!entity.isEntityInvulnerable(source) && entity.isEntityAlive())
        {
            if (entity instanceof EntityLivingBase)
            {
                if (entity instanceof EntityPlayer)
                {
                    if (((EntityPlayer) entity).capabilities.isCreativeMode)
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
        this.setDead();
    }

    /**
     * Sets the position based on the bounding box
     */
    protected void alignToBounds()
    {
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = this.getEntityBoundingBox().minY + (double) this.getYOffset() - (double) this.height;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }

    /**
     * Gets the predicted position
     *
     * @param t - number of ticks to predicted
     * @return predicted position of the project
     */
    public Pos getPredictedPosition(int t)
    {
        Pos newPos = new Pos((Entity) this);

        for (int i = 0; i < t; i++)
        {
            newPos.add(motionX, motionY, motionZ);
        }

        return newPos;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        setHealth(nbt.getFloat(NBTConstants.HEALTH));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setFloat(NBTConstants.HEALTH, this.getHealth());
    }

    @Override
    public World world()
    {
        return world;
    }

    @Override
    public double x()
    {
        return posX;
    }

    @Override
    public double y()
    {
        return posY;
    }

    @Override
    public double z()
    {
        return posZ;
    }

    public Pos getVelocity()
    {
        return new Pos(motionX, motionY, motionZ); //TODO make wrapper object
    }
}
