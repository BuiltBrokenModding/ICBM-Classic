package icbm.classic.content.explosive.blast;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IMissile;
import icbm.classic.client.models.ModelICBM;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.explosive.ExplosiveHandler;
import icbm.classic.content.explosive.thread.ThreadExplosion;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Prefab for any Explosion/Blast object created
 */
public abstract class Blast extends Explosion implements IBlast
{
    public ThreadExplosion thread;
    //TODO remove position as we are double storing location data
    public Location position;
    public EntityExplosion controller = null;

    public boolean isAlive = true;

    /** The amount of times the explosion has been called */
    protected int callCount = 0;

    public Blast(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size, false, true);
        this.position = new Location(world, x, y, z);
    }

    public Blast(Location pos, Entity entity, float size)
    {
        super(pos.world(), entity, pos.x(), pos.y(), pos.z(), size, false, true);
        this.position = pos;
    }

    public Blast(Entity entity, float size)
    {
        super(entity.world, entity, entity.posX, entity.posY, entity.posZ, size, false, true);
        this.position = new Location(entity);
    }

    protected void doPreExplode()
    {
    }

    /** Called before an explosion happens. */
    public final void preExplode()
    {
        ExplosiveHandler.add(this);
        this.doPreExplode();
    }

    /** Called every tick when this explosive is being progressed. */
    protected abstract void doExplode();

    public final void onExplode()
    {
        this.doExplode();
        this.callCount++;
    }

    protected void doPostExplode()
    {
    }

    /** Called after the explosion is completed. */
    public final void postExplode()
    {
        this.doPostExplode();
        ExplosiveHandler.remove(this);
    }

    /**
     * Called each tick the blast has moved
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public void onPositionUpdate(double posX, double posY, double posZ)
    {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        position = new Location(world(), posX, posY, posZ);
    }

    /** Make the default functions useless. */
    @Override
    public void doExplosionA()
    {
    }

    @Override
    public void doExplosionB(boolean par1)
    {
    }

    /** All outside classes should call this. */
    public void explode()
    {
        if (this.proceduralInterval() > 0)
        {
            if (!this.world().isRemote)
            {
                this.world().spawnEntity(new EntityExplosion(this));
            }
        }
        else
        {
            this.doPreExplode();
            this.doExplode();
            this.doPostExplode();
        }
    }

    public int countIncrement()
    {
        return 1;
    }


    public float getBlastRadius()
    {
        return Math.max(3, this.size);
    }

    @Deprecated //TODO remove
    public long getEnergy()
    {
        return 0;
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need procedural calls
     */
    public int proceduralInterval()
    {
        return -1;
    }

    protected void doDamageEntities(float radius, float power)
    {
        this.doDamageEntities(radius, power, true);
    }

    protected void doDamageEntities(float radius, float power, boolean destroyItem)
    {
        // Step 2: Damage all entities
        radius *= 2.0F;
        Location minCoord = position.add(-radius - 1);
        Location maxCoord = position.add(radius + 1);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minCoord.xi(), minCoord.yi(), minCoord.zi(), maxCoord.xi(), maxCoord.yi(), maxCoord.zi()));
        Vec3d var31 = new Vec3d(position.x(), position.y(), position.z());

        for (int i = 0; i < allEntities.size(); ++i)
        {
            Entity entity = allEntities.get(i);

            if (this.onDamageEntity(entity))
            {
                continue;
            }

            if (entity instanceof IMissile)
            {
                ((IMissile) entity).destroyMissile(true);
                continue;
            }

            if (entity instanceof EntityItem && !destroyItem)
            {
                continue;
            }

            double distance = entity.getDistance(position.x(), position.y(), position.z()) / radius;

            if (distance <= 1.0D)
            {
                double xDifference = entity.posX - position.x();
                double yDifference = entity.posY - position.y();
                double zDifference = entity.posZ - position.z();
                double var35 = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
                xDifference /= var35;
                yDifference /= var35;
                zDifference /= var35;
                double var34 = world().getBlockDensity(var31, entity.getEntityBoundingBox());
                double var36 = (1.0D - distance) * var34;
                int damage = 0;

                damage = (int) ((var36 * var36 + var36) / 2.0D * 8.0D * power + 1.0D);

                entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), damage);

                entity.motionX += xDifference * var36;
                entity.motionY += yDifference * var36;
                entity.motionZ += zDifference * var36;
            }
        }
    }

    /**
     * Called by doDamageEntity on each entity being damaged. This function should be inherited if
     * something special is to happen to a specific entity.
     *
     * @return True if something special happens to this specific entity.
     */
    protected boolean onDamageEntity(Entity entity)
    {
        return false;
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.callCount = nbt.getInteger("callCount");
        this.size = nbt.getFloat("explosionSize");
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("callCount", this.callCount);
        nbt.setFloat("explosionSize", this.size);
    }

    public boolean isMovable()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public ModelICBM getRenderModel()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getRenderResource()
    {
        return null;
    }

    @Override
    public World world()
    {
        return this.position.world();
    }

    @Override
    public double x()
    {
        return this.position.x();
    }

    @Override
    public double y()
    {
        return this.position.y();
    }

    @Override
    public double z()
    {
        return this.position.z();
    }

    @Override
    public Entity getBlastSource()
    {
        return getBlastSource();
    }
}
